package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerWebSocket {


    public static void main(String[] args) {
        int port = 8000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur web démarré sur le port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion entrante");

                managerRequest(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Elle permettrat de gere la connexion et l'interaction des nouvelle client avec le server
     *
     * @param clientSocket
     * @throws IOException
     */
    private static void managerRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();

        String requestLine = in.readLine();
        System.out.println("Requête du client : " + requestLine);

        String[] requestParts = requestLine.split(" ");
        String method = requestParts[0];
        String path = requestParts[1];
        System.out.println("Method: " + method);
        if ("GET".equals(method)) {
            System.out.println("Path: " + path);
            GetRequestManager(path, out);

        } else {
            sendResponse(501, "La method " + method + " n'est pas encore implementer", out);
        }

        in.close();
        out.close();
        clientSocket.close();
    }

    /**
     * Elle permettra de recupere la page html corespondand a l'url chercher par l'utilisateur
     *
     * @param path
     * @param out
     * @throws IOException
     */
    private static void GetRequestManager(String path, OutputStream out) throws IOException {

        //        if(path.split(".")[1].equals(".html")){
        File file;
        if (path.contains(".html")) {
            file = new File("www" + path);

        } else {
            if (path.equals("/")) {
                System.out.println("Request: " + path.length() + "==" + path.equals("/"));
                file = new File("www" + "/index" + ".html");
            } else {
                file = new File("www" + path + ".html");
            }

        }

        if (file.exists() && file.isFile()) {
            System.out.println("File : " + file.getAbsolutePath());
            String contentType = guessContentType(file);
            String response = readFileContent(file);
            sendResponse(200, "OK", contentType, response, out);
        } else {
            sendResponse(404, "Not Found", out);
        }
    }

    /**
     * Elle permettra d'ajouter le content type a l'url avant de renvoie la page corespondante
     *
     * @param file
     * @return String
     */
    private static String guessContentType(File file) {
        String fileName = file.getName();
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return "text/html";
        } else if (fileName.endsWith(".css")) {
            return "text/css";
        } else if (fileName.endsWith(".js")) {
            return "application/javascript";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * Permet de lire le contenue du fichier html chercher
     *
     * @param file
     * @return StringBuilder
     * @throws IOException
     */
    private static String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }


    /**
     * Permet de renvoie les pages dont les status code d'errores (404,401,500,501...)
     * Aussi d'ajouter des content types et d'ajouter de charsetName aux differente reponse
     * contenues Et de vide le canal de communication apres avoir retourner la reponse au client HTTP
     *
     * @param statusCode
     * @param statusText
     * @param out
     * @throws IOException
     */
    private static void sendResponse(int statusCode, String statusText, OutputStream out) throws IOException {
        String response = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n\r\n";
        out.write(response.getBytes("UTF-8"));
        out.flush();
    }

    /**
     * Elee permettrat de renvoie les pages dont le status code est 200
     * Aussi d'ajouter des content types et d'ajouter de charsetName aux differente reponse plus leurs contenues
     * Et de vide le canal de communication apres avoir retourner la reponse au client HTTP
     *
     * @param statusCode
     * @param statusText
     * @param contentType
     * @param content
     * @param out
     * @throws IOException
     */
    private static void sendResponse(int statusCode, String statusText, String contentType, String content, OutputStream out) throws IOException {
        String response = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n";
        response += "Content-Type: " + contentType + "\r\n";
        response += "Content-Length: " + content.length() + "\r\n\r\n";
        response += content;

        out.write(response.getBytes("UTF-8"));
        out.flush();
    }
}

