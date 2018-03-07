/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finalprojectserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import java.io.ObjectOutputStream;
import java.util.List;
import simulation.Simulation;
import physics.Point;

/**
 *
 * @author Owen
 */
public class FXMLDocumentControllerServer implements Initializable {
    
    private int player=0;
    private Simulation sim;
    @FXML
    private TextArea textArea;
    

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sim = new Simulation(1000, 800, 2, 2);
        new Thread( () -> {
            try {
                ServerSocket serverSocket = new ServerSocket(8000);
                while (true) {
                    Socket socket = serverSocket.accept();
                    Platform.runLater( () -> {
                        textArea.appendText("Player " + String.valueOf(player+1) + " joined.\n");
                    });
                    new Thread(new handlePlayer(socket,textArea,sim,player)).start();
                    player++;
                }
            }catch(IOException ex) {
                Platform.runLater( () -> {
                        textArea.appendText("Player " + String.valueOf(player+1) + " left.\n");
                    });
                player--;
            }
            
    }).start();
    }    
    
}

class handlePlayer implements Runnable, net.NetConstants{
    private Socket socket;
    private Simulation sim;
    private TextArea textArea;
    private boolean ready1;
    private int player;
    public handlePlayer(Socket socket, TextArea textArea, Simulation sim, int player){
        this.socket=socket;
        this.textArea = textArea;
        this.sim = sim;
        this.player=player;
    }
    @Override
    public synchronized void run(){
        try{
            BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outputToClient = new PrintWriter(socket.getOutputStream());
            ObjectOutputStream ObjOut = new ObjectOutputStream(socket.getOutputStream());
            while (true) {
              int request = Integer.parseInt(inputFromClient.readLine());
              switch(request) {
                  case GET_PADDLES: {                      
                      List list = sim.getPaddlePosition();
                      if(player==0){
                        ObjOut.writeObject(list.get(0));
                        ObjOut.writeObject(list.get(1));
                      }else{
                          ObjOut.writeObject(list.get(1));
                          ObjOut.writeObject(list.get(0));
                      }
                      ObjOut.flush();
                      break;
                      }
                  case GET_BALLS: {
                      Point pb = sim.getBallPosition();
                      ObjOut.writeObject(pb);
                      ObjOut.flush();
                      break;
                  }
                  case SEND_MOVES: {
                      int movesx = Integer.parseInt(inputFromClient.readLine());
                      int movesy = Integer.parseInt(inputFromClient.readLine());
                      sim.moveInner(movesx, movesy,player);
                      break;
                    }
                  case SEND_READY: {
                      String r = inputFromClient.readLine();
                      if(r.equals("ready")){
                          ready1=true;
                          
                      }else{
                          ready1=false;
                      }
                      outputToClient.println(player);
                      outputToClient.flush();
                      break;
                    }
                  case START_SIM: { 
                      Platform.runLater(()->textArea.appendText("Starting game\n"));
                      sim.setUpShapes();
                       new Thread(() -> {
                            while (true) {
                            sim.evolve(1.0);
                            Platform.runLater(()->sim.updateShapes());
                            try {
                                Thread.sleep(15);
                            } catch (InterruptedException ex) {}
                            }
                        }).start();
                       break;
                    }
                  

                  }

            } 
        }catch(IOException ex) {
          Platform.runLater(()->textArea.appendText("Exception in client thread: "+ex.toString()+"\n"));
          
      }
    }
    

}