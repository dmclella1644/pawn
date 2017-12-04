package jwtc.android.chess;


import android.graphics.Point;
import android.support.constraint.solver.widgets.Rectangle;
import android.util.Size;

import java.io.BufferedReader;
import java.io.File;

/**
 * Created by David on 12/3/2017.
 */

public class DataLabeler{
//    public static int[] orderCorners(int[] corners){
//        int[] temp = new int[4];
//        for(int i = 0; i < corners.length; i++){
//            temp[i] = corners[i];
//        }
//        //do later
//        int[][] xSorted = new int[][];
//        xSorted = temp[np.argsort(temp[:, 0]), :]
//        leftMost = xSorted[:2, :]
//        rightMost = xSorted[2:, :]
//
//        leftMost = leftMost[np.argsort(leftMost[:, 1]), :]
//        (tl, bl) = leftMost
//
//                D = dist.cdist(tl[np.newaxis], rightMost, "euclidean")[0]
//        (br, tr) = rightMost[np.argsort(D)[::-1], :]
//        return np.float32([tl.tolist(),tr.tolist(),br.tolist(),bl.tolist()])
//    }
//    private static int[][] board_corners = new int[4][2];
//    private static Mat image;
//    private static String image_path;
//    private static String image_name;
//    private static Mat extended_transformed_image;
//    private static Mat transformed_perspective;
//    private static Mat extended_transformed_perspective;
//    private static boolean stop_picture = false;
//    private static String orientation;
//    private static Mat imagePoints;
//    private static Mat extended_image_points;
//    private static BufferedReader[][] boardArray = new BufferedReader[8][8];
//    private static Mat extended_corners;
//    public static void __init__(String imagePath, String imageName, String orientation, int[][] cornerPoints){
//        imagePoints = {{0,0},{640,0},{640,640},{640,640}};
//        extended_image_points = {{0,0},{640,0},{640,160},{0,160}};
//        image = initializeImage(imagePath);
//        image_path = imagePath;
//        image_name = imageName;
//        extended_transformed_image = null;
//        orientation = orientation;
//        transformed_perspective = null;
//        extended_transformed_perspective = null;
//        //this.labeler = Model();
//        for(int i = 0; i < 4; i++){
//            board_corners = orderCorners(cornerPoints);
//        }
//        applyTransformation();
//        splitAndLabel();
//    }
//    public static Mat initializeImage(String imagePath){
//        Mat read_original_temp = imread(imagePath);
//        int rows = read_original_temp.rows();
//        int cols = read_original_temp.cols();
//        Point center = new Point(cols / 2, rows / 2);
//        Mat read_orig = getRotationMatrix2D(center, 0.,1.);
//        Size size = new Size(new Point(cols,rows));
//        Mat img_rotation;
//        warpAffine(read_original_temp, img_rotation, read_orig, size);
//        Mat resized_Image;
//        resize(img_rotation, resized_Image, size);
//        return resized_Image;
//    }
//    public static void setPoints(int[] corners){
//        for(int i = 0; i < 4; i++){
//            for(int j = 0; j < 2; j++){
//                board_corners[i][j] = corners[i][j];
//            }
//        }
//    }
//    public static void applyTransformation(){
//        Mat transformed_perspective = getPerspectiveTransform(board_corners, imagePoints);
//        Mat tranformed_image;
//        Size dsize = new Size(new Point(640,640));
//        warpPerspective(image, tranformed_image, transformed_perspective, dsize);
//        imwrite("transformed.jpg", transformed_image);
//        extendImage();
//        Mat extended_transformed_perspective = getPerspectiveTransform(extended_corners, extended_image_points);
//        Mat extended_transformed_image;
//        Size esize = new Size(new Point(640,640));
//        warpPerspective(image, extended_transformed_perspective, esize);
//        imwrite("transformedE.jpg", extended_transformed_image);
//    }
//    public static double[][] calculatePoint(int corner_one, int corner_two){
//        double distance = Math.sqrt(Math.pow(board_corners[corner_one][0]-board_corners[corner_two][0],2.)+Math.pow(board_corners[corner_one][1]-board_corners[corner_two][1],2.))/8;
//        double slope = ((board_corners[corner_one][1] - board_corners[corner_two][1]) /
//                (board_corners[corner_one][0] - board_corners[corner_two][0]));
//        double xValue = board_corners[corner_one][0] + distance / Math.sqrt(1 + Math.pow(slope,2.));
//        double yValue = board_corners[corner_one][1] + distance * slope / Math.sqrt(1 + Math.pow(slope,2.));
//        double xValueTwo = board_corners[corner_one][0] - distance / Math.sqrt(1 + Math.pow(slope,2.));
//        double yValueTwo = board_corners[corner_one][1] - distance * slope / Math.sqrt(1 + Math.pow(slope,2.));
//        if(xValue < 0){
//            xValue = 0;
//        }
//        if(yValue < 0){
//            yValue = 0;
//        }
//        if(xValueTwo < 0){
//            xValueTwo = 0;
//        }
//        if(yValueTwo < 0){
//            yValueTwo = 0;
//        }
//        double[][] results = new double[2][2];
//        results[0][0] = xValue;
//        results[0][1] = yValue;
//        results[1][0] = xValueTwo;
//        results[1][1] = yValueTwo;
//        return results;
//    }
//    public static void extendImage(){
//        double[][] Left = calculatePoint(0,3);
//        double[][] Right = calculatePoint(1,2);
//        double[] topLeft = new double[2];
//        double[] bottomLeft = new double[2];
//        double[] topRight = new double[2];
//        double[] bottomRight = new double[2];
//        for(int i = 0; i < 2; i++){
//            topLeft[i] = Left[0][i];
//            bottomLeft[i] = Left[1][i];
//            topRight[i] = Right[0][i];
//            bottomRight[i] = Right[1][i];
//        }
//        double[][] allCorners = new double[4][2];
//        for(int i = 0; i < 2; i++){
//            allCorners[0][i] = topLeft[i];
//        }
//        for(int i = 0; i < 2; i++){
//            allCorners[1][i] = bottomLeft[i];
//        }
//        for(int i = 0; i < 2; i++){
//            allCorners[2][i] = topRight[i];
//        }
//        for(int i = 0; i < 2; i++){
//            allCorners[3][i] = bottomRight[i];
//        }
//        extended_corners = orderCorners(allCorners);
//    }
//    public static BufferedImage splitAndLabelHelper(int x, int nextX, int y, int nextY, int width, int height, int heightE, BufferedImage baseImage, BufferedImage baseImageE){
//        int start_x = x*width/8;
//        int start_y = y*height/8;
//        int end_x = nextX*width/8;
//        if(y == 0){
//            int end_y = heightE;
//            BufferedImage imageSquare = cropImage(baseImageE, Rectangle(start_x, start_y, end_x-start_x, end_y-start_y));
//        } else {
//            int end_y = nextY * width / 8;
//            BufferedImage imageSquare = cropImage(baseImageE, Rectangle(start_x, start_y-width/16, end_x-start_x, end_y-start_y+width/16));
//        }
//        imageSquare.save("square.jpg")
//        return this.labeler.labelImage("square.jpg")
//    }
//    private BufferedImage cropImage(BufferedImage src, Rectangle rect) {
//        BufferedImage dest = src.getSubimage(0, 0, rect.width, rect.height);
//        return dest;
//    }
//    public static void splitAndLabel(){
//        BufferedImage baseImage = ImageIO.read(new File("transformed.jpg"));
//        int width = baseImage.getWidth();
//        int height = baseImage.getHeight();
//        BufferedImage baseImageE = ImageIO.read(new File("transformed.jpg"));
//        int widthE = baseImageE.getWidth();
//        int heightE = baseImageE.getHeight();
//        int place = 0;
//
//        if(self.orientation == 'Top'){
//            for(int x = 7; x > -1; x--){
//                for(int y = 0; y < 8; y++){
//                    boardArray[(int)place/8][place%8] = splitAndLabelHelper(x-1, x, y, y+1, width, height, heightE, baseImage, baseImageE);
//                    place++;
//                }
//            }
//        }
//        if(self.orientation == 'Bottom'){
//            for(int x = 0; x < 8; x++){
//                for(int y = 8; y > 0; y--){
//                    boardArray[(int)place/8][place%8] = splitAndLabelHelper(x, x+1, y-1, y, width, height, heightE, baseImage, baseImageE);
//                    place++;
//                }
//            }
//        }
//        if(self.orientation == 'Right'){
//            for(int y = 8; y > 0; y--){
//                for(int x = 8; x > 0; x--){
//                    boardArray[(int)place/8][place%8] = splitAndLabelHelper(x-1, x, y-1, y, width, height, heightE, baseImage, baseImageE);
//                    place++;
//                }
//            }
//        }
//        if(self.orientation == 'Left'){
//            for(int y = 0; y < 8; y++){
//                for(int x = 0; x < 8; x++){
//                    boardArray[(int)place/8][place%8] = splitAndLabelHelper(x, x+1, y, y+1, width, height, heightE, baseImage, baseImageE);
//                    place++;
//                }
//            }
//        }
//    }
//    public static int[][] getLabels(){
//        return boardArray;
//    }
}
