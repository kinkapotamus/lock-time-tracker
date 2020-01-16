package com.locktimetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.locktimetracker.db.AppDatabase;
import com.locktimetracker.db.Session;
import com.locktimetracker.utils.CountUpTimer;

import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    AppDatabase db;
    CountUpTimer timer;

    Session currentSesh = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init DB
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "LockTimeTracker").allowMainThreadQueries().build();

        // checking for current session
        currentSesh = db.sessionDAO().getCurrentSession();

        // set total time (counting current session if available, up until now() )
        long totalTimeWithCurrentSession = getTotalTimeWithCurrentSessionIfActive(currentSesh);
        final TextView textView = findViewById(R.id.totalTime);
        textView.setText(timestampToText(totalTimeWithCurrentSession));

        // set locked percentage
        TextView percentageTextView = findViewById(R.id.percentageTime);
        percentageTextView.setText("(" + getLockedPercentage(totalTimeWithCurrentSession) + "% of the time)");

        // long click to toggle the action
        getLockButton().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleLock(null);
                return true;
            }
        });

        // creating the live timer
        timer = new CountUpTimer(1000000, totalTimeWithCurrentSession) { // big number to fake infinity..
            public void onTick(int second) {
                textView.setText(timestampToText(getStartFromInMs() + second * 1000));
            }
        };

        // doing things if session is active
        if (currentSesh != null) {
            displayUnlockButton();
            timer.start();
        }
    }

    private void lock() {
        // insert
        Session sesh = new Session();
        sesh.startDate = new Date();
        db.sessionDAO().insert(sesh);

        // update timer
        if (currentSesh == null) {
            currentSesh = db.sessionDAO().getCurrentSession();
        }
        timer.setStartFromInMs(getTotalTimeWithCurrentSessionIfActive(currentSesh));
        timer.start();

        // change button state
        displayUnlockButton();
    }

    private void unlock() {
        if (currentSesh == null) {
            currentSesh = db.sessionDAO().getCurrentSession();
        }
        if (currentSesh != null) { // just in case the DB returns null...
            currentSesh.endDate = new Date();
            db.sessionDAO().update(currentSesh);
            timer.cancel(); // stopping the live timer
            displayLockButton();
            currentSesh = null; // reset current sesh
        } else {
//            System.out.println("No current session found");
        }
    }

    private long getTotalTimeWithCurrentSessionIfActive(Session currentSesh) {
        long totalTime = db.sessionDAO().getTotalLockedTime();
        long timeToAdd = 0;
        if (currentSesh != null) {
            Date now = new Date();
            timeToAdd = (now.getTime() - currentSesh.startDate.getTime());
        }
        return totalTime + timeToAdd;
    }

    public void toggleLock(View view) {
        if ("Lock".equals(getLockButton().getText())) {
            lock();
        } else {
            unlock();
        }
    }

    public String timestampToText(long timestamp) {
        long diffSeconds = timestamp / 1000 % 60;
        long diffMinutes = timestamp / (60 * 1000) % 60;
        long diffHours = timestamp / (60 * 60 * 1000) % 24;
        long diffDays = timestamp / (24 * 60 * 60 * 1000);

        return diffDays + " days " + diffHours + " hours " + diffMinutes + " minutes " + diffSeconds + " seconds.";
    }

    private Button getLockButton() {
        Button lockButton = findViewById(R.id.button);
        if (lockButton == null) {
//            System.out.println("button cannot be found");
            return null;
        } else {
            return lockButton;
        }
    }

    private void displayLockButton() {
        getLockButton().setText(R.string.lock_button);
        getLockButton().setBackgroundColor(0xFF99CC00);
    }

    private void displayUnlockButton() {
        getLockButton().setText(R.string.unlock_button);
        getLockButton().setBackgroundColor(0xFFFF4444);
    }

    private long getLockedPercentage(long totalTimeLockedWithActiveSession) {
        Date startOfYear = new GregorianCalendar(2020, 0, 1, 0, 0).getTime();
        Date now = new Date();
        long timeElapsedSinceStartOfYearInMs = now.getTime() - startOfYear.getTime();

        return (long) ((totalTimeLockedWithActiveSession / (float) timeElapsedSinceStartOfYearInMs) * 100);

    }
}
