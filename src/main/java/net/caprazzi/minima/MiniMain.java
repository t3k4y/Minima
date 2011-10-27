package net.caprazzi.minima;

import net.caprazzi.keez.simpleFileDb.KeezFileDb;
import net.caprazzi.minima.service.MinimaDb;
import net.caprazzi.minima.service.MinimaPushService;
import net.caprazzi.minima.service.MinimaService;
import net.caprazzi.minima.servlet.MinimaCometServlet;
import net.caprazzi.minima.servlet.MinimaIndexServlet;
import net.caprazzi.minima.servlet.MinimaLoginServlet;
import net.caprazzi.minima.servlet.MinimaWebsocketServlet;
import net.caprazzi.minima.servlet.PrivacyFilter;

public class MiniMain {

	public static void main(String[] args) throws Exception {
		
		int port = Integer.parseInt(System.getProperty("minima.port", "8989"));		
		String dbDir = System.getProperty("minima.db.dir", "./minima-db");
		String dbPrefix = System.getProperty("minima.db.prefix", "minimav0");
		String boardTitle = System.getProperty("minima.board.default.title", "Minima");
		String password = System.getProperty("minima.password");
		String publicView = System.getProperty("minima.readonly", "false");
		
		boolean isPrivate = (password != null && password.length() > 0);
		boolean hasPublicView = publicView.equalsIgnoreCase("true");
		
		KeezFileDb db = new KeezFileDb(dbDir, dbPrefix, true);
		db.setAutoPurge(true);
		
		MinimaIndexServlet indexServlet = new MinimaIndexServlet();
		indexServlet.setTitle(boardTitle);
		MinimaWebsocketServlet websocketServlet = new MinimaWebsocketServlet();
		MinimaCometServlet cometServlet = new MinimaCometServlet();
		
		MinimaPushService pushService = new MinimaPushService(websocketServlet, cometServlet);
		
		PrivacyFilter privacyFilter = new PrivacyFilter(isPrivate, hasPublicView);
		MinimaLoginServlet loginServlet = null;
		
		if (isPrivate) {
			loginServlet = new MinimaLoginServlet(password);
		}
		
		MinimaDb minimaDbHelper = new MinimaDb(db);
		minimaDbHelper.init();
		MinimaService minimaService = new MinimaService(db, pushService);
		MinimaServer minimaServer = new MinimaServer(
				minimaService,
				websocketServlet, 
				cometServlet, 
				indexServlet,
				loginServlet,
				privacyFilter);
		minimaServer.start(port);
	}
}