/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tick.tack.toe.client.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.awt.TrayIcon.MessageType;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tick.tack.toe.client.TickTackToeClient;
import tick.tack.toe.client.controllers.server.ServerListener;
import tick.tack.toe.client.models.Invitation;
import tick.tack.toe.client.models.Match;
import tick.tack.toe.client.models.Player;
import tick.tack.toe.client.models.PlayerFullInfo;
import tick.tack.toe.client.notifications.AskToResumeNotification;
import tick.tack.toe.client.requests.AcceptInvitationRequest;
import tick.tack.toe.client.requests.AcceptToResumeRequest;
import tick.tack.toe.client.requests.GetMatchHistoryRequest;
import tick.tack.toe.client.requests.InviteToGameRequest;
import tick.tack.toe.client.requests.RejectInvitationRequest;
import tick.tack.toe.client.requests.RejectToResumeRequest;
import tick.tack.toe.client.responses.AskToResumeResponse;
import tick.tack.toe.client.responses.InviteToGameResponse;
import tick.tack.toe.client.responses.Response;




public class HomeViewController implements Initializable {

    private Map<Integer, PlayerFullInfo> playersFullInfo;
    private PlayerFullInfo myPlayerFullInfo;
    private Map<Integer, Invitation> invitations;
    private Map<Integer, Player> sent;
    
    @FXML
    private TableView<PlayerFullInfo> tPlayers;
    @FXML
    private TableColumn<PlayerFullInfo, String> cPlayerName;
    @FXML
    private TableColumn<PlayerFullInfo, String> cStatus;
    @FXML
    private TableColumn<PlayerFullInfo, String> cIsInGame;
    @FXML
    private TableView<Invitation> tInvitation;
    @FXML
    private TableColumn<Invitation, String> cFrom;
    @FXML
    private TableColumn<Invitation, String> cNotif;
    
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnMatches;
    @FXML
    private Button btnInvite;
    @FXML
    private Button vsComputerButton;
    @FXML
    private Label lblName;
    @FXML
    private Label lblScore;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        invitations = new HashMap<>();
        sent = new HashMap<>();
        
        cPlayerName.setCellValueFactory(new PropertyValueFactory<>("Name"));
        cStatus.setCellValueFactory(new PropertyValueFactory<>("Status"));
        cIsInGame.setCellValueFactory(new PropertyValueFactory<>("InGame")); 
        //tPlayers.setItems();
        
        cFrom.setCellValueFactory(new PropertyValueFactory<>("Name"));
        cNotif.setCellValueFactory(new PropertyValueFactory<>("Type"));
        
        tInvitation.setRowFactory(tv -> {
            TableRow<Invitation> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    if(tInvitation.getSelectionModel().getSelectedItem().getType() == Invitation.GAME_INVITATION)
                        showInvitationConfirmation();
                    else
                        respondToResumeReq();
                }
            });
            return row;
        });

    }
    // to confirm user
    private void showInvitationConfirmation() {
        Invitation invitation = tInvitation.getSelectionModel().getSelectedItem();
        System.out.println("HomeViewController -> showInvitationConfirmation: "+ invitation.getPlayer().getDb_Player_id());
        System.out.println("HomeViewController -> showInvitationConfirmation: "+ invitation.getPlayer().getServer_id());
        if (invitation.getType().equals(Invitation.GAME_INVITATION)) {
            confirmGameInvitation(invitation);
        }
    }
    private void confirmGameInvitation(Invitation invitation) {
        
                System.out.println("HomeViewController -> confirmGameInvitation: "+ invitation.getPlayer().getServer_id());
                System.out.println("HomeViewController -> confirmGameInvitation: "+ invitation.getPlayer().getDb_Player_id());
        if (TickTackToeClient.showConfirmation(invitation.getType(), invitation.getName() + " invite you to a game.", "Accept", "Decline")) {
            // accept the invitation
            playersFullInfo.get(invitation.getPlayer().getDb_Player_id()).setServer_id(invitation.getPlayer().getServer_id());
            AcceptInvitationRequest acceptInvitationReq = new AcceptInvitationRequest(new Player(playersFullInfo.get(invitation.getPlayer().getDb_Player_id())));
            try {
                System.out.println("HomeViewController -> confirmGameInvitation: "+ acceptInvitationReq.getPlayer().getServer_id());
                System.out.println("HomeViewController -> confirmGameInvitation: "+ acceptInvitationReq.getPlayer().getDb_Player_id());
                // create the json
                String jRequest = TickTackToeClient.mapper.writeValueAsString(acceptInvitationReq);
                ServerListener.sendRequest(jRequest);
                //TickTackToeClient.gameController.setCompetitor(invitation.getPlayer());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else {
            // Reject the invitation
            RejectInvitationRequest rejectInvitationReq = new RejectInvitationRequest(new Player(playersFullInfo.get(invitation.getPlayer().getDb_Player_id())));
            try {
                String jRequest = TickTackToeClient.mapper.writeValueAsString(rejectInvitationReq);
                ServerListener.sendRequest(jRequest);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        // remove the invitation from the map
        invitations.remove(invitation.getPlayer().getDb_Player_id());
        // update the table
        fillInvitationsTable();
    }
    @FXML protected void onActionCloseBtn (ActionEvent event){
        Platform.exit();
    }
    @FXML protected void onActionMinBtn (ActionEvent event){
        Stage stage = (Stage)((Button)event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    protected void onActionLogin(ActionEvent event) {

        TickTackToeClient.openLoginView();
    }
    @FXML protected void onActionMatch(ActionEvent event) {

        System.out.println("pressed Match");
        try {
            GetMatchHistoryRequest getMatchHistoryReq = new GetMatchHistoryRequest();
            String jRequest = TickTackToeClient.mapper.writeValueAsString(getMatchHistoryReq);
            ServerListener.sendRequest(jRequest);
            TickTackToeClient.openMatchView();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    @FXML protected void onActionVsComputer(ActionEvent event) {

        System.out.println("pressed VsComputer");
        TickTackToeClient.openGameVsComputerView();
    }
    @FXML protected void onActionInvitePlayer(ActionEvent event) {

        System.out.println("pressed Invite Player");
        // get selected object
        PlayerFullInfo playerFullInfo = tPlayers.getSelectionModel().getSelectedItem();
        System.out.println("HomeViewController -> onActionInvitePlayer: "+playerFullInfo.getDb_Player_id());
        // check if the selected object is valid and not sent him invite before
        if (isValidSelection(playerFullInfo) && sent.get(playerFullInfo.getDb_Player_id()) == null) {
            // create invite to a game request
            InviteToGameRequest inviteToGameReq = new InviteToGameRequest(new Player(playerFullInfo));
            try {
                // convert the request to string
                String jRequest = TickTackToeClient.mapper.writeValueAsString(inviteToGameReq);
                // send the request
                ServerListener.sendRequest(jRequest);
                // add request to sent
                sent.put(playerFullInfo.getDb_Player_id(), playerFullInfo);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
    private boolean isValidSelection(PlayerFullInfo playerFullInfo) {
        boolean valid = false;
        if (playerFullInfo == null) {
            TickTackToeClient.showAlert("Error", "You have to select a player first", Alert.AlertType.ERROR);
            // if selected player is in game
        } else if (playerFullInfo.isInGame()) {
            TickTackToeClient.showAlert("Error", "You have to select a player which is not in game", Alert.AlertType.ERROR);
            // if selected player is offline
        } else if (playerFullInfo.getStatus().equals(PlayerFullInfo.OFFLINE)) {
            TickTackToeClient.showAlert("Error", "You have to select an online player", Alert.AlertType.ERROR);
        } else {
            valid = true;
        }
        return valid;
    }
    
    /**
     * Start Functionality
     */
    public void fromLogin(PlayerFullInfo myPlayerFullInfo, Map<Integer, PlayerFullInfo> playersFullInfo) {
        this.myPlayerFullInfo = myPlayerFullInfo;
        this.playersFullInfo = playersFullInfo;
        System.out.println(myPlayerFullInfo.getDb_Player_id());
        System.out.println(playersFullInfo.values());
        playersFullInfo.remove(myPlayerFullInfo.getDb_Player_id());
        fillView();
//        sent.clear();
        offline(false);
    }
    private void fillView() {
        fillPlayersTable();
        lblName.setText(myPlayerFullInfo.getName());
        lblScore.setText(String.valueOf(myPlayerFullInfo.getPoints()));
    }
    private void fillPlayersTable() {
        tPlayers.getItems().clear();
        tPlayers.getItems().setAll(playersFullInfo.values());

        tPlayers.getSortOrder().add(cStatus);
    }
    public void offline(boolean isOffline) {
        tInvitation.setDisable(isOffline);
        tPlayers.setDisable(isOffline);
        btnMatches.setDisable(isOffline);
        btnInvite.setDisable(isOffline);
        vsComputerButton.setDisable(isOffline);
        showHideLoginBtn(isOffline);
    }
    public void showHideLoginBtn(boolean isOffline){
        btnLogin.setVisible(myPlayerFullInfo == null && !isOffline);
    }
    
    public PlayerFullInfo getMyPlayerFullInfo() {
        return myPlayerFullInfo;
    }
    
    public void updateStatus(PlayerFullInfo playerFullInfo) {
        if (playersFullInfo != null) {
            if (playerFullInfo.getDb_Player_id()== myPlayerFullInfo.getDb_Player_id()) {

                lblScore.setText(String.valueOf(playerFullInfo.getPoints()));
                //TickTackToeClient.showAlert("Back Online", "You are now online", Alert.AlertType.INFORMATION);
            } else {
                if (!playerFullInfo.getStatus().equals(playersFullInfo.get(playerFullInfo.getDb_Player_id()).getStatus())) {
                    if(playerFullInfo.getStatus().equals(PlayerFullInfo.ONLINE)){
                        playersFullInfo.get(playerFullInfo.getDb_Player_id()).setStatus(PlayerFullInfo.ONLINE);
                    } else {
                        playersFullInfo.get(playerFullInfo.getDb_Player_id()).setStatus(PlayerFullInfo.OFFLINE);
                    }
                }
                //playersFullInfo.put(playerFullInfo.getDb_Player_id(), playerFullInfo);
                fillPlayersTable();
            }
        }
    }
    
    public void inviteToGameResponse(InviteToGameResponse inviteToGameRes) {
        if (inviteToGameRes.getStatus().equals(Response.STATUS_ERROR)) {
            TickTackToeClient.showAlert("Invite To game", inviteToGameRes.getMessage(), Alert.AlertType.WARNING);
        }
        sent.remove(inviteToGameRes.getPlayer().getDb_Player_id());
    }
    
    public void addResumeReq(AskToResumeNotification askToResumeNotification){
        if (invitations.get(askToResumeNotification.getPlayer().getDb_Player_id()) == null) {
            Invitation invitation = new Invitation(Invitation.RESUME_INVITATION, askToResumeNotification.getPlayer(), askToResumeNotification.getMatch());
            invitation.setName(playersFullInfo.get(askToResumeNotification.getPlayer().getDb_Player_id()).getName());
            invitations.put(askToResumeNotification.getPlayer().getDb_Player_id(), invitation);
            fillInvitationsTable();

            TickTackToeClient.showAlert("Game Invitation", 
                    playersFullInfo.get(askToResumeNotification.getPlayer().getDb_Player_id()).getName() + " sent you game invitation."
                    ,Alert.AlertType.INFORMATION);
        }
    }
    
    public void respondToResumeReq()
     {
         Player player = tInvitation.getSelectionModel().getSelectedItem().getPlayer();
         Match match = tInvitation.getSelectionModel().getSelectedItem().getMatch();
         if(TickTackToeClient.showConfirmation("ShowNotification","Do you want to resume game ?","Accept","Reject"))
         {
                         System.out.println("respondToResumeReq 1: ");

            System.out.println("xxxxxxxxxx: "+match.getPlayer1_id());
            System.out.println("uuuu: "+match.getPlayer2_id());
            System.out.println("oooo: "+match.getPlayer1_choice());
            System.out.println("respond: "+match.getPlayer2_choice());

            AcceptToResumeRequest acceptToResumeReq = new AcceptToResumeRequest(player, match);
            try {
                String jRequest = TickTackToeClient.mapper.writeValueAsString(acceptToResumeReq);
                ServerListener.sendRequest(jRequest);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
         }
         else{
                                      System.out.println("respondToResumeReq 2: ");

             RejectToResumeRequest rejectToResumeReq = new RejectToResumeRequest(player);
             try {
                 String jRequest = TickTackToeClient.mapper.writeValueAsString(rejectToResumeReq);
                 ServerListener.sendRequest(jRequest);
             } catch (JsonProcessingException e) {
                 e.printStackTrace();
             }
         }
         invitations.remove(player.getDb_Player_id());
         fillInvitationsTable();
     }

    public void declineResume(AskToResumeResponse askToResumeRes){
        TickTackToeClient.showAlert("Resume Game, Declined!", 
            getPlayerFullInfo(askToResumeRes.getPlayer().getDb_Player_id()).getName()+" cannot resume game right now."
            ,Alert.AlertType.INFORMATION);
    }
    
    public PlayerFullInfo getPlayerFullInfo(int id) {
        if (myPlayerFullInfo.getDb_Player_id()== id) {
            return myPlayerFullInfo;
        }
        return playersFullInfo.get(id);
    }

    public void startGame(Match match) {
        sent.clear();
        TickTackToeClient.gameController.startMatch(match);
        TickTackToeClient.openGameView();
    }
    
    public void notifyGameInvitation(Player player) {
        // check if received this notification before
        if (invitations.get(player.getDb_Player_id()) == null) {
            Invitation invitation = new Invitation();
            invitation.setType(Invitation.GAME_INVITATION);
            invitation.setPlayer(player);
            invitation.setName(playersFullInfo.get(player.getDb_Player_id()).getName());
            invitations.put(invitation.getPlayer().getDb_Player_id(), invitation);
            
            fillInvitationsTable();
//            TickTackToeClient.showSystemNotification("Game Invitation",
//                    playersFullInfo.get(player.getDb_Player_id()).getName() + " sent you game invitation.",
//                    MessageType.INFO);
        }
    }
    private void fillInvitationsTable() {
        tInvitation.getItems().clear();
        tInvitation.getItems().setAll(invitations.values());
    }
}

