/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tick.tack.toe.server.models;

/**
 *
 * @author booga
 */
public class Position {
    private int match_id, player_id;
    private String position, type;

    public Position() {
    }

    public Position(int match_id, int player_id, String position) {
        this.match_id = match_id;
        this.player_id = player_id;
        this.position = position;
    }
    public Position(int match_id, int player_id, String position, String type) {
        this.match_id = match_id;
        this.player_id = player_id;
        this.position = position;
        this.type = type;
    }

    public int getMatch_id() {
        return match_id;
    }

    public void setMatch_id(int match_id) {
        this.match_id = match_id;
    }

    public int getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(int player_id) {
        this.player_id = player_id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
    
     public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    
}
