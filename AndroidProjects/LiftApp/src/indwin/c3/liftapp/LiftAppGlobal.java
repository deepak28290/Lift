package indwin.c3.liftapp;

import android.app.Application;

public class LiftAppGlobal extends Application {

	    private boolean sessionActive;

	    public boolean getSessionActive() {
	        return sessionActive;
	    }

	    public void setSessionActive(boolean sessionActive) {
	        this.sessionActive = sessionActive;
	    }
}
