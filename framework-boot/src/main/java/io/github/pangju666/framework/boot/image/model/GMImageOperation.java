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

import javax.annotation.Nullable;
import java.util.Objects;

public class GMImageOperation extends ImageOperation {
	private Boolean stripProfiles;
	private Integer quality;

	public static GMImageOperation.GMImageOperationBuilder builder() {
		return new GMImageOperation.GMImageOperationBuilder();
	}

	public @Nullable Integer getQuality() {
		return quality;
	}

	public @Nullable Boolean getStripProfiles() {
		return stripProfiles;
	}

	public static class GMImageOperationBuilder extends ImageOperationBuilder {
       private final GMImageOperation imageOperation = new GMImageOperation();

	   public GMImageOperationBuilder stripProfiles(Boolean stripProfiles) {
		   if (Objects.nonNull(stripProfiles)) {
			   imageOperation.stripProfiles = stripProfiles;
		   }
		   return this;
	   }

		public GMImageOperationBuilder quality(Integer quality) {
			if (Objects.nonNull(quality) && quality > 0) {
				imageOperation.quality = quality;
			}
			return this;
		}

	   public GMImageOperation build() {
           return imageOperation;
       }
   }
}
