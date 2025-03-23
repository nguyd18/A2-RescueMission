package ca.mcmaster.se2aa4.island.team023;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;

public class Map implements IMap {

	private List<List<Cell>> grid = new ArrayList<>();

	private int h = 0;
	private int w = 0;

	private boolean isOcean;
	private boolean fromScan;
	private boolean radarGroundFound;
	private int distance = 0;

	private String[] creeks;
	private String[] sites;

	/**
	 * @see IMap#placeCell()
	 */
	public void placeCell(int x, int y, int nextX, int nextY, JSONObject results) {
		
		// no need to place cell if no scan or echo used
		if (!results.has("extras")) return;

		parseJSON(results);
		if (fromScan) {

			expandMap(x + distance * nextX, y + distance * nextY);

			// loop through all points from (x, y) to the end of the radar ping and mark as ocean
			int i = 1;
			while (x + i < x + distance * nextX - 1 || y + i < y + distance * nextY - 1) {
				grid.get(y).set(x, new OceanCell(x + i, y + i));
				i++;
			}

			// if the pinged point is ground, mark it, otherwise ignore OOB
			if (radarGroundFound) grid.get(y).set(x, new GroundCell(x + i, y + i));
			
		} else {
			expandMap(x, y);
			if (isOcean) grid.get(y).set(x, new OceanCell(x, y));
			else grid.get(y).set(x, new DetailedGroundCell(x, y, creeks, sites));
		}
		
		
	}

	private void expandMap(int x, int y) {
		// expand map if too narrow
		while (x >= w) {
			for (List<Cell> row : grid) {
				row.add(null);
			}
			w++;
		}
		
		// expand map if too short
		while (y >= h) {
			ArrayList<Cell> row = new ArrayList<>();
			while (row.size() < w) row.add(null);
			grid.add(row);
			h++;
		}
	}

	private void parseJSON(JSONObject input) {

		isOcean = true;
		fromScan = false;
		radarGroundFound = false;
		distance = 0;

		JSONObject extras;
		if (input.has("extras")) extras = input.getJSONObject("extras");
		else return;

		// echo response case
		if (extras.has("range") && extras.has("found")) {
			fromScan = true;
			distance = extras.getInt("range");

			radarGroundFound = extras.get("found").equals("GROUND");
			return;
		}

		// scan response case
		if (extras.has("biomes") && extras.has("creeks") && extras.has("sites")) {

			JSONArray contents = extras.getJSONArray("biomes");

			// check if ocean is the only listed biome => prioritize ground if it exists
			for (int i = 0; i < contents.length();i++) {
				isOcean = isOcean && contents.getString(i).equals("OCEAN");
			}

			// collect all creeks
			contents = extras.getJSONArray("creeks");
			creeks = new String[contents.length()];
			for (int i=0;i < creeks.length;i++) {
				creeks[i] = contents.getString(i);
			}

			// collect all sites
			contents = extras.getJSONArray("sites");
			sites = new String[contents.length()];
			for (int i=0;i < sites.length;i++) {
				sites[i] = contents.getString(i);
			}
			
		}

	}


	/**
	 * @see IMap#getCell()
	 */
	public Cell getCell(int x, int y) {

		// consider raising error instead
		if (y >= h || x >= w) return null;
		return grid.get(y).get(x);
	}

	public int getWidth() {
		return w;
	}
	
	public int getHeight() {
		return w;
	}

}
