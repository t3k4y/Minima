package net.caprazzi.minima;

import net.caprazzi.minima.service.MinimaService.CreateStory;
import net.caprazzi.minima.service.MinimaService.UpdateStory;

public class TestUtils {
	
	public final static CreateStory createStoryNoop = new CreateStory() {
		@Override
		public void error(String string, Exception e) {}
		
		@Override
		public void success(byte[] story) {}
	};
	
	public final static UpdateStory updateStoryNoop = new UpdateStory() {
		@Override
		public void error(String string, Exception e) {}
		
		@Override
		public void success(String key, int rev, byte[] data) {}

		@Override
		public void collision(String key, int yourRev, int foundRev) {}
	};
	
	public static class TestCreateStory extends CreateStory {

		@Override
		public void error(String string, Exception e) {
			throw new RuntimeException("unexpected error");
		}

		@Override
		public void success(byte[] story) {
			throw new RuntimeException("unexpected success");
		}
	}
	
	public static class TestUpdateStory extends UpdateStory {
		@Override
		public void error(String string, Exception e) {
			throw new RuntimeException("unexpected error");
		}

		@Override
		public void success(String key, int rev, byte[] data) {
			throw new RuntimeException("unexpected success");
		}

		@Override
		public void collision(String key, int yourRev, int foundRev) {
			throw new RuntimeException("unexpected collision");
		}
	}
	
}
