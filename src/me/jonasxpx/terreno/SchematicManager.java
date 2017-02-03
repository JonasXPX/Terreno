package me.jonasxpx.terreno;

import java.io.File;
import java.io.IOException;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.data.DataException;

public class SchematicManager {
	
	
	private Vector vector;
	private File schematicFile;
	
	public SchematicManager(Vector vector, String schematicName) {
		this.vector = vector;
		this.schematicFile = new File(Terreno.instance.getWorldEdit().getDataFolder() + "/schematics/" + schematicName + ".schematic");
	}
	
	@SuppressWarnings("deprecation")
	public void loadSchematic(LocalWorld world) throws DataException, IOException, MaxChangedBlocksException{
		EditSession es = new EditSession(world, 99999999);
		CuboidClipboard cc;
		try {
			cc = CuboidClipboard.loadSchematic(schematicFile);
			cc.paste(es, vector, false);
			es.flushQueue();
		} catch (MaxChangedBlocksException e) {
			e.printStackTrace();
		}
	
	}
	
}
