/* -*- Mode: C++; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/*
 * This file is part of the LibreOffice project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.libreoffice.impressremote.communication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Messenger;

public class CommunicationService extends Service implements Runnable {

    public enum State {
        DISCONNECTED, SEARCHING, CONNECTING, CONNECTED
    };

    /**
     * Used to protect all writes to mState, mStateDesired, and mServerDesired.
     */
    private Object mConnectionVariableMutex = new Object();

    private State mState = State.DISCONNECTED;

    private State mStateDesired = State.DISCONNECTED;

    private Server mServerDesired = null;

    @Override
    public void run() {
        synchronized (this) {
            while (true) {
                // Condition
                try {

                    wait();
                } catch (InterruptedException e) {
                    // We have finished
                    return;
                }
                // Work
                synchronized (mConnectionVariableMutex) {
                    if ((mStateDesired == State.CONNECTED && mState == State.CONNECTED)
                                    || (mStateDesired == State.DISCONNECTED && mState == State.CONNECTED)) {
                        mClient.closeConnection();
                        mState = State.DISCONNECTED;
                    }
                    if (mStateDesired == State.CONNECTED) {
                        switch (mServerDesired.getProtocol()) {
                        case NETWORK:
                            mClient = new NetworkClient(
                                            mServerDesired.getAddress(), this);
                            mTransmitter = new Transmitter(mClient);
                            mClient.setReceiver(mReceiver);
                            break;
                        case BLUETOOTH:
                            break;
                        }
                        mState = State.CONNECTED;
                    }
                }
            }
        }

    }

    public void startSearching() {
        synchronized (mConnectionVariableMutex) {
            if (mState == State.CONNECTING || mState == State.CONNECTED) {
                disconnect();
            }
            mFinder.startFinding();
            mState = State.SEARCHING;
        }
    }

    public void stopSearching() {
        synchronized (mConnectionVariableMutex) {
            mFinder.stopFinding();
            mState = State.DISCONNECTED;
        }
    }

    public void connectTo(Server aServer) {
        synchronized (mConnectionVariableMutex) {
            if (mState == State.SEARCHING) {
                mFinder.stopFinding();
                mState = State.DISCONNECTED;
            }
            mServerDesired = aServer;
            mStateDesired = State.CONNECTED;
            synchronized (this) {
                notify();
            }

        }
        // TODO: connect
    }

    public void disconnect() {
        synchronized (mConnectionVariableMutex) {
            mStateDesired = State.DISCONNECTED;
            synchronized (this) {
                notify();
            }
        }
    }

    /**
     * Return the service to clients.
     */
    public class CBinder extends Binder {
        public CommunicationService getService() {
            return CommunicationService.this;
        }
    }

    private final IBinder mBinder = new CBinder();

    public static final int MSG_SLIDESHOW_STARTED = 1;
    public static final int MSG_SLIDE_CHANGED = 2;
    public static final int MSG_SLIDE_PREVIEW = 3;
    public static final int MSG_SLIDE_NOTES = 4;

    public static final String MSG_SERVERLIST_CHANGED = "SERVERLIST_CHANGED";
    public static final String MSG_PAIRING_STARTED = "PAIRING_STARTED";
    public static final String MSG_PAIRING_SUCCESSFUL = "PAIRING_SUCCESSFUL";

    private Transmitter mTransmitter;

    private Client mClient;

    private Receiver mReceiver = new Receiver();

    private ServerFinder mFinder = new ServerFinder(this);

    public void setActivityMessenger(Messenger aActivityMessenger) {
        mReceiver.setActivityMessenger(aActivityMessenger);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return mBinder;
    }

    private Thread mThread = null;

    @Override
    public void onCreate() {
        // TODO Create a notification (if configured).
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void onDestroy() {
        // TODO Destroy the notification (as necessary).
        mThread.interrupt();
        mThread = null;
    }

    public Transmitter getTransmitter() {
        return mTransmitter;
    }

    public Server[] getServers() {
        return mFinder.getServerList();
    }

    public void startFindingServers() {
        mFinder.startFinding();
    }

    public void stopFindingServers() {
        mFinder.stopFinding();
    }

    public SlideShow getSlideShow() {
        return mReceiver.getSlideShow();
    }

}
/* vim:set shiftwidth=4 softtabstop=4 expandtab: */