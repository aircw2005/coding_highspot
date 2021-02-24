import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayListApp {
    public static void main(String[] args) throws IOException {
        System.out.println("Argument 1: json file for playlist/player/songs.");
        System.out.println("Argument 2: json file for changes.");
        System.out.println("Argument 3: json file for output.");

        if (args.length < 3) {
            System.out.println("Not enough arguments, exit");
            return;
        }

        String playListFilePath = args[0];

        String playListFileData = new String(Files.readAllBytes(
                Paths.get(playListFilePath)));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        PlayListInfo playListInfo = gson.fromJson(playListFileData, PlayListInfo.class);
        if (playListInfo == null) {
            System.out.println("Play list data file is not valid, exit.");
            return;
        }

        String changesFilePath = args[1];

        String changeFileData = new String(Files.readAllBytes(
                Paths.get(changesFilePath)));

        Change[] changes = gson.fromJson(changeFileData, Change[].class);
        if (changes == null) {
            System.out.println("Changes data file is not valid, exit.");
            return;
        }

        // Populate all playlist data to a map.
        Map<String, PlayList> playListMap = new HashMap();
        for (PlayList playList: playListInfo.playlists) {
            playListMap.put(playList.id, playList);
        }

        for (Change change: changes) {
            String type = change.type.toLowerCase();
            if (type.equals("addplaylist")) {
                playListMap.put(change.playList.id, change.playList);
            } else if (type.equals("removeplaylist")) {
                playListMap.remove(change.playListId);
            } else if (type.equals("addsong")) {
                PlayList playList = playListMap.get(change.playListId);
                if (playList == null) {
                    System.out.println("The play list does not exist for id: " + playList.id);
                    continue;
                }
                playList.song_ids.add(change.song_id);
                playListMap.put(change.playListId, playList);
            }
        }

        PlayListInfo newPlayListInfo = new PlayListInfo();
        newPlayListInfo.users = playListInfo.users;
        newPlayListInfo.playlists = new ArrayList();
        for (String listId: playListMap.keySet()) {
            newPlayListInfo.playlists.add(playListMap.get(listId));
        }

        newPlayListInfo.songs = playListInfo.songs;

        String outputFilePath = args[2];
        System.out.println("Writing output to file: " + outputFilePath);
        Gson outputGson = new GsonBuilder().setPrettyPrinting().create();

        Writer writer = new FileWriter(outputFilePath);
        outputGson.toJson(newPlayListInfo, writer);
        writer.close();
    }
}
