import java.io.IOException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

public class ApiExtractor {

    private static final String[] MAPPING_ANNOTATIONS = {
            "GetMapping", "PostMapping", "PutMapping", "DeleteMapping", "RequestMapping"
    };

    public static void main(String[] args) {
        System.out.println("About to start");
        Path repositoryPath = Paths.get("/home/administrator/Nunbaa_Projects/blueprints-uat/5");
        List<String> apis = extractApisFromDirectory(repositoryPath);
        int count = 0;
        for (String api : apis) {
            System.out.println("-------------> count : " + (++count));
            System.out.println(api);
        }
    }

    public static List<String> extractApisFromDirectory(Path directory) {
        List<String> allApis = new ArrayList<>();

        try {
            Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> allApis.addAll(extractApisFromFile(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return allApis;
    }

    public static List<String> extractApisFromFile(Path filePath) {
        List<String> apis = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            StringBuilder fileContent = new StringBuilder();
            for (String line : lines) {
                fileContent.append(line).append(System.lineSeparator());
            }

            for (String annotation : MAPPING_ANNOTATIONS) {
                Pattern pattern = Pattern.compile("@" + annotation + "\\s*\\(.*?\\)", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(fileContent.toString());

                while (matcher.find()) {
                    String api = matcher.group();
                    int methodStartIndex = matcher.end();
                    int methodEndIndex = findMethodEndIndex(fileContent, methodStartIndex);
                    String methodSignature = fileContent.substring(methodStartIndex, methodEndIndex);
                    apis.add("Annotation: " + annotation + ", Method: " + methodSignature.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return apis;
    }

    private static int findMethodEndIndex(StringBuilder fileContent, int methodStartIndex) {
        int openBrackets = 0;
        boolean methodStarted = false;

        for (int i = methodStartIndex; i < fileContent.length(); i++) {
            char ch = fileContent.charAt(i);
            if (ch == '{') {
                openBrackets++;
                methodStarted = true;
            } else if (ch == '}') {
                openBrackets--;
                if (methodStarted && openBrackets == 0) {
                    return i + 1;
                }
            }
        }
        return fileContent.length();
    }
}
