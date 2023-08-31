/**
 * COMP90015: Distributed Systems - Assignment 1
 * File: DictionaryDataHandler.java
 *
 * Author: Flynn Schneider
 * Student ID: 982143
 * Date: 31/8/23
 *
 * Description: Handles the loading of data from, and storing to, a dedicated Dictionary JSON file.
 * Usage: [Indirect. Called by DictionaryServer upon Thread start.]
 */

package Server;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DictionaryDataHandler {

    /* JSON / data Specific structures and methods */
    public ConcurrentHashMap<String, String> loadDictionaryFromFile(String filePath) throws IOException {
        ConcurrentHashMap<String, String> dictionary = new ConcurrentHashMap<>();
        String json_content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONObject jsonObject = new JSONObject(json_content);

        for (String key : jsonObject.keySet()) {
            dictionary.put(key, jsonObject.getString(key));
        }

        return dictionary;
    }

    public void saveDictionaryToFile(ConcurrentHashMap<String, String> dictionary, String filePath) throws IOException {
        JSONObject jsonObject = new JSONObject();

        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            jsonObject.put(entry.getKey(), entry.getValue());
        }
        Files.write(Paths.get(filePath), jsonObject.toString(4).getBytes()); // 4 sets the indent factor
    }
}
