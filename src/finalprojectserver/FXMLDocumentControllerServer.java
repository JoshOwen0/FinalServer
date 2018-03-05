/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finalprojectserver;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.shape.Shape;
import simulation.Simulation;

/**
 *
 * @author Owen
 */
public class FXMLDocumentControllerServer implements Initializable {
    
    private Simulation sim;
    @FXML
    private TextArea textArea;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sim = new Simulation(600, 500, 2, 2);
        new Thread( () -> {
            try {
                ServerSocket serverSocket = new ServerSocket(8000);
                while (true) {
                    Socket socket = serverSocket.accept();
                    Platform.runLater( () -> {
                        textArea.appendText("New Player Joined\n");
                    });
                    new Thread(new handlePlayer(socket,textArea,sim)).start();
                }
            }catch(IOException ex) {
                ex.printStackTrace();
            }
            
    }).start();
    }    
    
}

class handlePlayer implements Runnable, net.NetConstants{
    private Socket socket;
    private Simulation sim;
    private TextArea textArea;
    private Gson gson;
    public handlePlayer(Socket socket, TextArea textArea, Simulation sim){
        this.socket=socket;
        this.textArea = textArea;
        this.sim = sim;
        gson = new Gson();
    }
    @Override
    public void run(){
        try{
            BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outputToClient = new PrintWriter(socket.getOutputStream());

            while (true) {
              int request = Integer.parseInt(inputFromClient.readLine());
              switch(request) {
                  case GET_SHAPES: {
                      //get from sim, need to think of a way to encode the data
                      List<Shape> shapes = sim.setUpShapes();   //Gson doesn't like Shapes
                      Shape sha = shapes.get(0);
                      String st = gson.toJson(sha);
                      System.out.println(st);
                      outputToClient.println(shapes.size());
                      for(int i=0;i<shapes.size();i++){
                          String s = gson.toJson(shapes.get(i));
                          outputToClient.println(s);
                      }
                      outputToClient.flush();
                      break;
                      }
                  case SEND_MOVES: {
                      String moves = inputFromClient.readLine();
                      //interpret and pass to sim
                      break;
                    }
                  case SEND_READY: {
                      String r = inputFromClient.readLine();
                      if(r.equals("ready")){
                          //Pass to sim
                      }else{
                          //pass to sim
                      }
                      break;
                    }

                  }

            } 
        }catch(IOException ex) {
          Platform.runLater(()->textArea.appendText("Exception in client thread: "+ex.toString()+"\n"));
          
      }
    }
    

}