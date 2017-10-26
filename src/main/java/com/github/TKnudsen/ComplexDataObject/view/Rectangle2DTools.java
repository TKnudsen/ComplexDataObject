package com.github.TKnudsen.ComplexDataObject.view;

import java.awt.geom.Rectangle2D;

/**
 * <p>
 * Title: Rectangle2DTools
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2017
 * </p>
 * 
 * @author Juergen Bernard
 */
public class Rectangle2DTools {

	public static Rectangle2D[][] createRectangleMatrix(Rectangle2D rectangle, int xCount, int yCount, double betweenSpaceOffset) {
		if (rectangle == null || xCount <= 0 || yCount <= 0 || betweenSpaceOffset < 0 || rectangle.getWidth() < xCount + betweenSpaceOffset * (xCount - 1) || rectangle.getHeight() < yCount + betweenSpaceOffset * (yCount - 1))
			return null;

		betweenSpaceOffset = 2;
		
		Rectangle2D[][] rectangleArray = new Rectangle2D[xCount][yCount];

		double ySpace = rectangle.getHeight() - ((yCount-1) * betweenSpaceOffset);
		double xSpace = rectangle.getWidth() - ((xCount-1) * betweenSpaceOffset);

		double height =Math.floor(ySpace / yCount);
		double width = Math.floor(xSpace / xCount);

		for (int x = 0; x < xCount; x++) {
			for (int y = 0; y < yCount; y++) {
				double xPosition = rectangle.getX() + x * width + (x - 1) * betweenSpaceOffset;
				double yPosition = rectangle.getY() + y * height + (y - 1) * betweenSpaceOffset;

				rectangleArray[x][y] = new Rectangle2D.Double(xPosition, yPosition, width, height);

			}
		}

		return rectangleArray;
	}

	public static Rectangle2D[][] createRectangleMatrix(Rectangle2D rectangle, int xCount, int yCount, int betweenSpaceOffset) {

		return createRectangleMatrix(rectangle, xCount, yCount, (double) betweenSpaceOffset);

	}
}
