package net.caprazzi.minima;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

import net.caprazzi.keez.Keez;
import net.caprazzi.keez.simpleFileDb.KeezFileDb;
import net.caprazzi.minima.service.MinimaService;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

public class MinimaServiceFunctionalTest {

	private Keez.Db db;
	private MinimaService service;
	private File testDir;
	private boolean flag;

	@Before
	public void setUp() {
		testDir = Files.createTempDir();
		db = new KeezFileDb(testDir.getAbsolutePath(), "pfx");
		service = new MinimaService(db, null);
	}
	
	@Test
	public void create_story_should_return_story_with_id_and_rev() {
		Story story = new Story(null,"desc","list");
		service.createStory("id", service.asJson(story), new TestUtils.TestCreateStory() {
			
			@Override
			public void success(Story stored) {
				assertEquals("desc", stored.getDesc());
				assertEquals("list", stored.getList());
				assertNotNull(stored.getId());
				assertEquals(1, stored.getRevision());
				flag = true;
			}
		});
		assertTrue(flag);
	}
	
	@Test
	public void update_story_should_return_story_with_id_and_rev() {
		Story story = new Story(null,"desc","list");
		
		service.createStory("id", service.asJson(story), new TestUtils.TestCreateStory() {
			@Override
			public void success(Story created) {
				created.setDesc("newDesc");
				created.setList("newList");
				byte[] updatedJson = service.asJson(created);
				service.updateStory(created.getId(), created.getRevision(), updatedJson, new TestUtils.TestUpdateStory() {
					@Override
					public void success(String key, int revision, byte[] jsonData) {
						Story updated = service.fromJson(jsonData);
						assertEquals("newDesc", updated.getDesc());
						assertEquals("newList", updated.getList());
						assertEquals(key, updated.getId());
						assertEquals(revision, updated.getRevision());
						flag = true;
					}
				});
			}
		});
		
		assertTrue(flag);
	}
	
	@Test
	public void created_stories_should_be_in_board() throws JsonParseException, IOException {
		Story story = new Story(null,"desc","list");
		service.createStory("id", service.asJson(story), TestUtils.createStoryNoop);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter out = new OutputStreamWriter(baos);
		service.writeBoard(out);
		System.out.println(baos.toString());
		
		Board b = new ObjectMapper().readValue(baos.toString(), Board.class);

		assertEquals(1, b.getStories().size());
		
		Story s = b.getStories().toArray(new Story[]{})[0];
		
		assertEquals(1, s.getRevision());
		assertEquals("desc", s.getDesc());
		assertEquals("list", s.getList());
		assertNotNull(s.getId());
	}
	
	@Test
	public void updated_stories_should_be_in_board() throws JsonParseException, IOException {
		Story story = new Story(null,"desc","list");
		
		service.createStory("id", service.asJson(story), new TestUtils.TestCreateStory() {
			@Override
			public void success(Story updated) {
				updated.setDesc("newDesc");
				updated.setList("newList");
				byte[] updatedJson = service.asJson(updated);
				service.updateStory(updated.getId(), updated.getRevision(), updatedJson, new TestUtils.TestUpdateStory() {
					@Override
					public void success(String key, int rev, byte[] data) {
						flag = true;
					}
				});
			}
		});
		
		assertTrue(flag);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter out = new OutputStreamWriter(baos);
		service.writeBoard(out);
		System.out.println(baos.toString());
		
		Board b = new ObjectMapper().readValue(baos.toString(), Board.class);

		assertEquals(1, b.getStories().size());
		
		Story s = b.getStories().toArray(new Story[]{})[0];
		
		assertEquals(2, s.getRevision());
		assertEquals("newDesc", s.getDesc());
		assertEquals("newList", s.getList());
		assertNotNull(s.getId());
	}
	
	private static class Board {
		
		private Collection<Story> stories;
		
		public Collection<Story> getStories() {
			return stories;
		}
		
		public void setStories(Collection<Story> stories) {
			this.stories = stories;
		}
		
	}
	
}
