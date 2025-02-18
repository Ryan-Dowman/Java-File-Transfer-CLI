import java.io.Serializable;
import java.util.List;

public class FilesAndDirectories implements Serializable{
    public List<String> files;
    public List<String> directories;

    public FilesAndDirectories(List<String> filePaths, List<String> directoryPaths) {
        this.files = filePaths;
        this.directories = directoryPaths;
    }
}
