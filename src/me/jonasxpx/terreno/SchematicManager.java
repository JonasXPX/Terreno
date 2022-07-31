package me.jonasxpx.terreno;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.data.DataException;

import java.io.File;
import java.io.IOException;

public class SchematicManager {
	
	private final Vector vector;
	private final File schematicFile;

	public SchematicManager(final Vector vector, final String schematicName) {
		this.vector = vector;
		this.schematicFile = new File(Terreno.we.getDataFolder() + "/schematics/" + schematicName + ".schematic");
	}
	
	public void loadSchematic(final LocalWorld world) throws DataException, IOException, MaxChangedBlocksException{
		final EditSession es = new EditSession(world, 99999999);
		final CuboidClipboard cc;
		try {
			cc = CuboidClipboard.loadSchematic(schematicFile);
			cc.paste(es, vector, false);
			es.flushQueue();
		} catch (MaxChangedBlocksException e) {
			e.printStackTrace();
		}
	
	}
	
}
