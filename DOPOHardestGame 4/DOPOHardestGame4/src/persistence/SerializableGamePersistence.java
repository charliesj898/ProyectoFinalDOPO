package persistence;

import domain.Game;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Implementación de GamePersistence basada en la serialización nativa de Java.
 * Guarda y carga el objeto Game completo en un archivo binario.
 */
public class SerializableGamePersistence implements GamePersistence {

    @Override
    public void save(Game game, String filename) throws Exception {
        try (FileOutputStream fileOut = new FileOutputStream(filename);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(game);
        }
    }

    @Override
    public Game load(String filename) throws Exception {
        try (FileInputStream fileIn = new FileInputStream(filename);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return (Game) in.readObject();
        }
    }
}
