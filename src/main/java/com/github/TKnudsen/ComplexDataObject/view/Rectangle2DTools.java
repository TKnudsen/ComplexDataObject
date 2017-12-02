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
 * @version 0.0.2
 */
public class Rectangle2DTools {

	public static Rectangle2D[][] createRectangleMatrix(Rectangle2D rectangle, int xCount, int yCount, double betweenSpaceOffset) {
		if (rectangle == null || xCount <= 0 || yCount <= 0 || betweenSpaceOffset < 0 || rectangle.getWidth() < xCount + betweenSpaceOffset * (xCount - 1) || rectangle.getHeight() < yCount + betweenSpaceOffset * (yCount - 1))
			return null;

		Rectangle2D[][] rectangleArray = new Rectangle2D[xCount][yCount];

		double ySpace = rectangle.getHeight() - ((yCount - 1) * betweenSpaceOffset);
		double xSpace = rectangle.getWidth() - ((xCount - 1) * betweenSpaceOffset);

		double height = ySpace / yCount;
		double width = xSpace / xCount;

		for (int x = 0; x < xCount; x++) {
			for (int y = 0; y < yCount; y++) {
				// bug fixed. position calculation was on ... (x-1)... earlier,
				// thus the whole arangement was shifted leftwards by
				// betweenSpaceOffset
				double xPosition = rectangle.getX() + x * width + x * betweenSpaceOffset;
				double yPosition = rectangle.getY() + y * height + y * betweenSpaceOffset;

				rectangleArray[x][y] = new Rectangle2D.Double(xPosition, yPosition, width, height);
			}
		}

		return rectangleArray;
	}

	public static Rectangle2D[][] createRectangleMatrix(Rectangle2D rectangle, int xCount, int yCount, int betweenSpaceOffset) {

		return createRectangleMatrix(rectangle, xCount, yCount, (double) betweenSpaceOffset);
	}
}
