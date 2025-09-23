/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.util.statistics;

/**
 * Implements a simple stopwatch.
 * 
 * RUNNING() == true <==> start() called with no corresponding call to stop()
 * 
 * All times are given in units of msec.
 */
public final class StopWatch
{
    private boolean _running;

    private long    _tStart;

    private long    _tFinish;

    private long    _tAccum;

    /**
     * Initializes Timer to 0 msec
     */
    public StopWatch( )
    {
        reset( );
    }

    /**
     * Starts the timer. Accumulates time across multiple calls to start.
     */
    public final void start( )
    {
        _running = true;
        _tStart = System.currentTimeMillis( );
        _tFinish = _tStart;
    }

    /**
     * Stops the timer. returns the time elapsed since the last matching call to start(), or zero if no such matching
     * call was made.
     */
    public final long stop( )
    {
        long diff = 0;
        _tFinish = System.currentTimeMillis( );
        if( _running) {
            _running = false;

            diff = _tFinish - _tStart;
            _tAccum += diff;
        }
        return diff;
    }

    /**
     * if RUNNING() ==> returns the time since last call to start() if !RUNNING() ==> returns total elapsed time
     */
    public final long elapsed( )
    {
        long elapsed = _tAccum;

        if( _running) {
            elapsed = System.currentTimeMillis( ) - _tStart;
        }

        return elapsed;
    }

    public final long getElapsed( )
    {
        return elapsed( );
    }

    /**
     * Stops timing, if currently RUNNING(); resets accumulated time to 0.
     */
    public final void reset( )
    {
        _running = false;
        _tStart = 0;
        _tFinish = 0;
        _tAccum = 0;
    }

}
