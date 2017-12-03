import os
import cv2
import numpy as np
import sys
from PIL import Image
from PIL import ImageEnhance
from scipy.spatial import distance as dist
from os import listdir
from os.path import isfile, join

from Model import Model


class DataLabeler:

    def __init__(self, imagePath, imageName, orientation, cornerPoints, imagePoints = np.array([[0,0], [640,0], [640,640], [0,640]], np.float32), 
                 extended_image_points = np.array([[0,0], [640,0], [640,160], [0,160]], np.float32)):

        self.image = self.initializeImage(imagePath)
        self.image_path = imagePath
        self.image_name = imageName
        self.transformed_image = None
        self.extended_transformed_image = None
        self.board_corners = cornerPoints
        self.orientation = orientation
        self.transformed_perspective = None
        self.extended_transformed_perspective = None
        self.imagePoints = imagePoints
        self.extended_image_points = extended_image_points
        self.extended_corners = []
        self.stop_picture = False
        self.boardArray = []
        self.labeler = Model()

        if len(self.board_corners) == 4:
            self.board_corners = self.orderCorners(self.board_corners)
            self.applyTransformation()
            self.splitAndLabel()
    
    def initializeImage(self, imagePath):
        read_original_temp = cv2.imread(imagePath)
        rows,cols = read_original_temp.shape[:2]
        read_orig = cv2.getRotationMatrix2D((cols/2,rows/2),0,1)
        img_rotation = cv2.warpAffine(read_original_temp, read_orig, (cols, rows))
        resized_Image = cv2.resize(img_rotation, (0,0), fx=0.25, fy=0.25) 
        return resized_Image

    def setPoints(self, corners):
        self.board_corners = corners

    def applyTransformation(self):
        self.transformed_perspective = cv2.getPerspectiveTransform(self.board_corners, self.imagePoints)
        self.transformed_image = cv2.warpPerspective(self.image, self.transformed_perspective, (640, 640))
        cv2.imwrite("transformed.jpg", self.transformed_image)

        self.extendImage()
        self.extended_transformed_perspective = cv2.getPerspectiveTransform(self.extended_corners, self.extended_image_points)
        self.extended_transformed_image = cv2.warpPerspective(self.image, self.extended_transformed_perspective, (640, 160))
        cv2.imwrite("transformedE.jpg", self.extended_transformed_image)

    def orderCorners(self, corners):
        temp = np.array(corners, dtype="int")
        xSorted = temp[np.argsort(temp[:, 0]), :]
        leftMost = xSorted[:2, :]
        rightMost = xSorted[2:, :]
     
        leftMost = leftMost[np.argsort(leftMost[:, 1]), :]
        (tl, bl) = leftMost
     
        D = dist.cdist(tl[np.newaxis], rightMost, "euclidean")[0]
        (br, tr) = rightMost[np.argsort(D)[::-1], :]
        return np.float32([tl.tolist(),tr.tolist(),br.tolist(),bl.tolist()])

    def calculatePoint(self, corner_one, corner_two):
        distance = dist.euclidean(self.board_corners[corner_one], self.board_corners[corner_two]) / 8
        slope = ((self.board_corners[corner_one][1] - self.board_corners[corner_two][1]) /
                 (self.board_corners[corner_one][0] - self.board_corners[corner_two][0]))
        xValue = self.board_corners[corner_one][0] + distance / np.sqrt(1 + slope**2)
        yValue = self.board_corners[corner_one][1] + distance * slope / np.sqrt(1 + slope**2)
        
        xValueTwo = self.board_corners[corner_one][0] - distance / np.sqrt(1 + slope**2)
        yValueTwo = self.board_corners[corner_one][1] - distance * slope / np.sqrt(1 + slope**2)

        if xValue < 0:
            xValue = 0
        if yValue < 0:
            yValue = 0
        if xValueTwo < 0:
            xValueTwo = 0
        if yValueTwo < 0:
            yValueTwo = 0
        return [np.int(xValue), np.int(yValue)], [np.int(xValueTwo), np.int(yValueTwo)]

    def extendImage(self):
        topLeft, bottomLeft = self.calculatePoint(0,3)
        topRight, bottomRight = self.calculatePoint(1,2)
        self.extended_corners = self.orderCorners([topLeft,topRight,bottomLeft, bottomRight])

    def splitAndLabelHelper(self, x, nextX, y, nextY, width, height, heightE, baseImage, baseImageE):
        start_x = x * width / 8
        start_y = y * height / 8
        end_x = nextX * width / 8
        
        if y == 0:
            end_y = heightE
            imageSquare = baseImageE.crop((start_x, start_y, end_x, end_y))

        else:
            end_y = nextY * width / 8
            imageSquare = baseImage.crop((start_x, start_y - width / 16, end_x, end_y))
            
        imageSquare.save("square.jpg")
        return self.labeler.labelImage("square.jpg")

    def splitAndLabel(self):
        w, h = 8, 8;
        self.boardArray = [[0 for x in range(w)] for y in range(h)]
        
        baseImage = Image.open("transformed.jpg")
        baseImage = baseImage.convert('RGB')

        baseImageE = Image.open("transformedE.jpg")
        baseImageE = baseImageE.convert('RGB')
  
        width, height = baseImage.size
        widthE, heightE = baseImageE.size
        place = 0

        if self.orientation == 'Top':
            for x in range(8, 0, -1):
                for y in range(0, 8):
                    self.boardArray[int(place/8)][place%8] = self.splitAndLabelHelper(x-1, x, y, y+1, width, height, heightE, baseImage, baseImageE)
                    place = place + 1

        if self.orientation == 'Bottom':
            for x in range(0, 8):
                for y in range(8, 0, -1):
                    self.boardArray[int(place/8)][place%8] = self.splitAndLabelHelper(x, x+1, y-1, y, width, height, heightE, baseImage, baseImageE)
                    place = place + 1

        if self.orientation == 'Right':
            for y in range(8, 0, -1):
                for x in range(8, 0, -1):
                    self.boardArray[int(place/8)][place%8] = self.splitAndLabelHelper(x-1, x, y-1, y, width, height, heightE, baseImage, baseImageE)
                    place = place + 1
                    
        if self.orientation == 'Left':
            for y in range(0, 8):
                for x in range(0, 8):
                    self.boardArray[int(place/8)][place%8] = self.splitAndLabelHelper(x, x+1, y, y+1, width, height, heightE, baseImage, baseImageE)
                    place = place + 1
                    
    def getLabels(self):
        return self.boardArray