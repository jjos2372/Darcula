/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bulenkov.iconloader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;

/**
 * @author Konstantin Bulenkov
 */
public class AppleHiDPIScaledImage {
  
	static Class<?> cImageClass = null;
  static {
	  try {
		cImageClass = Class.forName("apple.awt.CImage$HiDPIScaledImage");
	} catch (ClassNotFoundException e) {
	}
  }

  public static BufferedImage create(int width, int height, int imageType) {
	  if(cImageClass == null)
		  return null;
	
	try {
		Constructor<?> constructor = cImageClass.getConstructor(Integer.class, Integer.class, Integer.class);
		BufferedImage cimage = (BufferedImage) constructor.newInstance(width, height, imageType);
		
		return cimage;
	} catch (Exception e) {
		return null;
	}
	  
//    return new CImage.HiDPIScaledImage(width, height, imageType) {
//      @Override
//      protected void drawIntoImage(BufferedImage image, float scale) {
//      }
//    };
  }

  public static boolean is(Image image) {
    return cImageClass!=null && cImageClass.isInstance(image);
  }
}
