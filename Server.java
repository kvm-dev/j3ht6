package ru.geekbrains.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {
    private Vector<ClientHandler> clients;
    static Logger mylog = Logger.getLogger("mylog");
    Scanner scan = new Scanner(System.in);

    public Server() {
        clients = new Vector<>();
        ServerSocket server = null;
        Socket socket = null;
        try {
            AuthService.connect();
            server = new ServerSocket(8181);
            System.out.println("Сервер запущен. Ожидаем клиентов...");
            mylog.info("Сервачок стартанул");
            while (true) {
                socket = server.accept();
                mylog.info("Пользователь в чате");
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
                if (scan.nextLine().equals("/end")) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            mylog.error("Ошибка сервера:" + e.getMessage());
        } finally {
            try {
                socket.close();
                mylog.warn("Закрыли сокет сервера");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mylog.warn("Сервер отключен");
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void sendPersonalMsg(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nickTo)) {
                o.sendMsg("from " + from.getNick() + ": " + msg);
                from.sendMsg("to " + nickTo + ": " + msg);
                return;
            }
        }
        from.sendMsg("Клиент с ником " + nickTo + " не найден в чате");
    }

    public void broadcastMsg(ClientHandler from, String msg) {
        for (ClientHandler o : clients) {
            if (!o.checkBlackList(from.getNick())) {
                o.sendMsg(msg);
            }
        }
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientslist ");
        for (ClientHandler o : clients) {
            sb.append(o.getNick() + " ");
        }
        String out = sb.toString();
        for (ClientHandler o : clients) {
            o.sendMsg(out);
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientsList();
    }
}
