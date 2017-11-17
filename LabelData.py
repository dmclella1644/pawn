import matplotlib.pyplot as plt
import os
import skimage
import cv2
import numpy as np
import sys
from PIL import Image
from PIL import ImageEnhance
from scipy.spatial import distance as dist
from os import listdir
from os.path import isfile, join


class Board:

	def __init__(self,image, imageName, cornerPoints = [], imagePoints = np.array([[0,0], [640,0], [640,640], [0,640]], np.float32), 
				 extended_image_points = np.array([[0,0], [640,0], [640,160], [0,160]], np.float32)):
		self.image = image
		self.image_name = imageName
		self.transformed_image = None
		self.extended_transformed_image = None
		self.board_corners = cornerPoints
		self.transformed_perspective = None
		self.transformed_perspective_inv = None
		self.extended_transformed_perspective = None
		self.extended_transformed_perspective_inv = None
		self.imagePoints = imagePoints
		self.extended_image_points = extended_image_points
		self.extended_corners = []
		self.stop_picture = False

		if len(self.board_corners) == 4:
			self.board_corners = self.orderCorners(self.board_corners)
	

	def setPoints(self, corners):
		self.board_corners = corners

	def applyTransformation(self):
		print(self.board_corners)
		self.transformed_perspective = cv2.getPerspectiveTransform(self.board_corners, self.imagePoints)
		self.transformed_perspective_inv = np.linalg.inv(self.transformed_perspective)
		self.transformed_image = cv2.warpPerspective(self.image, self.transformed_perspective, (640, 640))
		cv2.imwrite(cut + self.image_name + "_transformed.jpg", self.transformed_image)

		self.extendImage()
		self.extended_transformed_perspective = cv2.getPerspectiveTransform(self.extended_corners, self.extended_image_points)
		self.extended_transformed_perspective_inv = np.linalg.inv(self.extended_transformed_perspective)
		self.extended_transformed_image = cv2.warpPerspective(self.image, self.extended_transformed_perspective, (640, 160))
		cv2.imwrite(cut + self.image_name + "_transformed_extended.jpg", self.extended_transformed_image)

	def clickPoints(self, event, x, y, flags, param):
		if event == cv2.EVENT_LBUTTONDOWN and len(self.board_corners) < 4:
			self.board_corners.append([x,y])
			print(self.board_corners)

	def selectPoints(self):
		self.board_corners = []
		clone = self.image.copy()
		cv2.namedWindow("image")
		cv2.setMouseCallback("image", self.clickPoints)

		while True:
			cv2.imshow("image", clone)
			key = cv2.waitKey(0)

			if key == ord("s"):
				self.stop_picture = True
				break

			if key == ord("c") and len(self.board_corners) == 4:
				break

			if key == ord("u") and self.board_corners > 0:
				del self.board_corners[-1]
				print(self.board_corners)

		if self.stop_picture == False:
			self.board_corners = self.orderCorners(self.board_corners)
		
		cv2.destroyAllWindows()

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

	def stopPicture(self):
		return self.stop_picture

	def isBlackPixel(self, pixel):
		return pixel[0] <= 175

	def isEmptySquare(self, image): #NOT DONE
		width, height = image.size
		image = ImageEnhance.Contrast(image).enhance(20)
		#Convert to greyscale
		image = image.convert('LA')
		pixels = image.load()

		highRowY = height * 4/5
		middleRowY = height * 3/5
		lowRowY = height * 2/5

		blackPixels = 0.0
		for i in range(int(width/4), int(width * 3/4)):
			if self.isBlackPixel(pixels[i, highRowY]):
				blackPixels += 1
			if self.isBlackPixel(pixels[i, middleRowY]):
				blackPixels += 1
			if self.isBlackPixel(pixels[i, lowRowY]):
				blackPixels += 1

		blackPixelRatio = blackPixels / (width * 3/2)
		return (blackPixelRatio < .1) or (blackPixelRatio > .9)

	def divideIntoSquaresAndSave(self, imageName, imageExtention):
		baseImage = Image.open(imageName + ".jpg")
		baseImage = baseImage.convert('RGB')

		baseImageExtention = Image.open(imageExtention +".jpg")
		baseImageExtention = baseImageExtention.convert('RGB')
  
		width, height = baseImage.size
		widthE, heightE = baseImageExtention.size
		fileNameIterator = 0
  
		for x in range(0, 8):
			for y in range(1, 8):
				startPixelX = x * width/8
				startPixelY = y * height/8
				endPixelX = (x+1) * width/8
				endPixelY = (y+1) * height/8
				squareImage = baseImage.crop((startPixelX, startPixelY, endPixelX, endPixelY))
				recImage = baseImage.crop((startPixelX, startPixelY - height/16, endPixelX, endPixelY))
				if not self.isEmptySquare(squareImage):
					recImage.save(unlabeled + self.image_name + str(fileNameIterator) + '.jpg')
					fileNameIterator += 1
			
			startPixelXE = x * widthE/8
			endPixelXE = (x+1) * widthE/8
			recImageE = baseImageExtention.crop((startPixelXE, 0, endPixelXE, heightE))
			recImageE.save(unlabeled + self.image_name + str(fileNameIterator) + '.jpg')
			fileNameIterator += 1



path = "/Users/Connor/Desktop/PAWN/RawData/"
done = "/Users/Connor/Desktop/PAWN/DoneTransforming/"
cut = "/Users/Connor/Desktop/PAWN/CutData/"
unlabeled = "/Users/Connor/Desktop/PAWN/Unlabeled/"
labeled = "/Users/Connor/Desktop/PAWN/Labeled/"
onlyfiles = []
datafiles = []

for f in os.listdir(path):
	if f.startswith('.'):
		continue
	elif isfile(join(path,f)):
		onlyfiles.append(f)


for file in onlyfiles:
	print(file)
	f = file.split('.')[0]
	read_original_temp = cv2.imread(path + file)
	rows,cols = read_original_temp.shape[:2]
	read_orig = cv2.getRotationMatrix2D((cols/2,rows/2),0,1)
	img_rotation = cv2.warpAffine(read_original_temp, read_orig, (cols, rows))
	read_original = img_rotation
	resized_Image = cv2.resize(read_original, (0,0), fx=0.25, fy=0.25) 
	testboard = Board(resized_Image, f)
	testboard.selectPoints()

	if testboard.stopPicture() == True:
		break

	testboard.applyTransformation()
	testboard.divideIntoSquaresAndSave(cut + f + "_transformed", cut + f + "_transformed_extended")
	os.rename(path+file, done+file)

for f in os.listdir(unlabeled):
	if f.startswith('.'):
		continue
	elif isfile(join(unlabeled,f)):
		datafiles.append(f)

stop = False
for file in datafiles:
	im = cv2.imread(unlabeled + file)
	if stop == True:
		break
	while True:
		cv2.imshow("image", im)
		key = cv2.waitKey(0)

		if key == ord("p") or key == ord("1"):
			os.rename(unlabeled+file, labeled + "/Pawn/" + file)
			break

		if key == ord("n") or key == ord("2"):
			os.rename(unlabeled+file, labeled + "/Knight/" + file)
			break

		if key == ord("b") or key == ord("3"):
			os.rename(unlabeled+file, labeled + "/Bishop/" + file)
			break

		if key == ord("r") or key == ord("4"):
			os.rename(unlabeled+file, labeled + "/Rook/" + file)
			break

		if key == ord("q") or key == ord("5"):
			os.rename(unlabeled+file, labeled + "/Queen/" + file)
			break

		if key == ord("k") or key == ord("6"):
			os.rename(unlabeled+file, labeled + "/King/" + file)
			break

		if key == ord("e") or key == ord("7"):
			os.rename(unlabeled+file, labeled + "/Empty/" + file)
			break

		if key == ord("g") or key == ord("8"):
			os.rename(unlabeled+file, labeled + "/Garbage/" + file)
			break

		if key == ord("s") or key == ord("9"):
			stop = True
			break

		else:
			continue

	cv2.destroyAllWindows()
	







