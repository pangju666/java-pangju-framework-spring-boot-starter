/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.framework.boot.image.model;

import com.twelvemonkeys.image.ResampleOp;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Objects;

public class BufferedImageOperation extends ImageOperation {
	private Integer scaleFilterType;

	public static BufferedImageOperation.BuffedImageOperationBuilder builder() {
		return new BufferedImageOperation.BuffedImageOperationBuilder();
	}

	public @Nullable Integer getScaleFilterType() {
		return scaleFilterType;
	}

   public static class BuffedImageOperationBuilder extends ImageOperationBuilder {
       private final BufferedImageOperation imageOperation = new BufferedImageOperation();

	   public BuffedImageOperationBuilder scaleFilterType(Integer filterType) {
		   if (Objects.nonNull(filterType) && filterType >= 0 && filterType <= 15) {
			   imageOperation.scaleFilterType = filterType;
		   }
		   return this;
	   }

	   public BuffedImageOperationBuilder scaleHints(Integer hints) {
		   if (Objects.nonNull(hints)) {
			   switch (hints) {
				   case Image.SCALE_FAST:
				   case Image.SCALE_REPLICATE:
					   imageOperation.scaleFilterType = ResampleOp.FILTER_POINT;
					   break;
				   case Image.SCALE_AREA_AVERAGING:
					   imageOperation.scaleFilterType = ResampleOp.FILTER_BOX;
					   break;
				   case Image.SCALE_SMOOTH:
					   imageOperation.scaleFilterType = ResampleOp.FILTER_LANCZOS;
					   break;
				   default:
					   break;
			   }
		   }
		   return this;
	   }

	   public BufferedImageOperation build() {
           return imageOperation;
       }
   }
}
