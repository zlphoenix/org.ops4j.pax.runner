/*
 * Copyright 2007 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.runner.platform.internal;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.Info;

/**
 * Stream related utilities.
 * TODO add units tests
 *
 * @author Alin Dreghiciu
 * @since August 19, 2007
 */
public class StreamUtils
{

    /**
     * Utility class. Ment to be used via static methods.
     */
    private StreamUtils()
    {
        // utility class
    }

    /**
     * Copy a stream to a destination. It does not close the streams.
     *
     * @param in          the stream to copy from
     * @param out         the stream to copy to
     * @param progressBar download progress feedback. Can be null.
     *
     * @throws IOException re-thrown
     */
    public static void streamCopy( final InputStream in, final BufferedOutputStream out, final ProgressBar progressBar )
        throws IOException
    {
        Assert.notNull( "Input stream", in );
        Assert.notNull( "Output stream", out );
        final long start = System.currentTimeMillis();
        int b = in.read();
        int counter = 0;
        int bytes = 0;
        ProgressBar feedbackBar = progressBar;
        if( feedbackBar == null )
        {
            feedbackBar = new NullProgressBar();
        }
        try
        {
            while( b != -1 )
            {
                out.write( b );
                b = in.read();
                counter = ( counter + 1 ) % 1024;
                if( counter == 0 )
                {
                    feedbackBar.increment( bytes, bytes / Math.max( System.currentTimeMillis() - start, 1 ) );
                }
                bytes++;
            }
        }
        finally
        {
            feedbackBar.increment( bytes, bytes / Math.max( System.currentTimeMillis() - start, 1 ) );
            feedbackBar.stop();
        }
    }

    /**
     * Copy a stream from an urlto a destination.
     *
     * @param url         the url to copy from
     * @param out         the stream to copy to
     * @param progressBar download progress feedback. Can be null.
     *
     * @throws IOException re-thrown
     */
    public static void streamCopy( final URL url, final BufferedOutputStream out, final ProgressBar progressBar )
        throws IOException
    {
        Assert.notNull( "URL", url );
        InputStream is = null;
        try
        {
            is = url.openStream();
            streamCopy( is, out, progressBar );
        }
        finally
        {
            if( is != null )
            {
                is.close();
            }
        }

    }

    /**
     * Feddback for downloading process.
     */
    public static interface ProgressBar
    {

        /**
         * Callback on download progress.
         *
         * @param bytes download size from when the download started
         * @param kbps  download speed
         */
        void increment( long bytes, long kbps );

        /**
         * Callback when download finished.
         */
        void stop();
    }

    /**
     * A progress bar that does nothing = does not display anything on console.
     */
    public static class NullProgressBar
        implements ProgressBar
    {

        public void increment( long bytes, long kbps )
        {
            // does nothing
        }

        public void stop()
        {
            // does nothing
        }

    }

    /**
     * A progress bar that displayed detailed information about downloading of an artifact
     */
    public static class FineGrainedProgressBar
        implements ProgressBar
    {

        /**
         * Name of the downloaded artifact.
         */
        private final String m_downloadTargetName;

        public FineGrainedProgressBar( final String downloadTargetName )
        {
            m_downloadTargetName = downloadTargetName;
            Info.print( downloadTargetName + " : connecting...\r" );
        }

        public void increment( final long bytes, final long kbps )
        {
            Info.print( m_downloadTargetName + " : " + bytes + " bytes @ [ " + kbps + "kBps ]\r" );
        }

        public void stop()
        {
            Info.println();
        }

    }

    /**
     * A progress bar that displayed corse grained information about downloading of an artifact
     */
    public static class CoarseGrainedProgressBar
        implements ProgressBar
    {

        /**
         * Name of the downloaded artifact.
         */
        private final String m_downloadTargetName;
        private long m_bytes;
        private long m_kbps;

        public CoarseGrainedProgressBar( final String downloadTargetName )
        {
            m_downloadTargetName = downloadTargetName;
            Info.print( downloadTargetName + " : downloading...\r" );
        }

        public void increment( final long bytes, final long kbps )
        {
            m_bytes = bytes;
            m_kbps = kbps;
        }

        public void stop()
        {
            Info.println( m_downloadTargetName + " : " + m_bytes + " bytes @ [ " + m_kbps + "kBps ]" );
        }

    }

}
