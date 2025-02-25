/*
 * Copyright (c) 2002-2021, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.asynchronousupload.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileItemHeaders;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.filesystem.UploadUtil;

/**
 * File item witch contains a list of partial file item
 */
public class PartialFileItemGroup implements FileItem<DiskFileItem>
{
    private static final long serialVersionUID = 8696893066570050604L;
    private List<FileItem<DiskFileItem>> _items;
    private SequenceInputStream _sequenceInputStream;

    /**
     * Instantiates a new normalize file item.
     *
     * @param item
     *            the item
     */
    public PartialFileItemGroup( List<FileItem<DiskFileItem>> items )
    {
        _items = items;
        List<InputStream> vOut = new ArrayList<>( );

        try
        {

            for ( FileItem<DiskFileItem> fileItem : items )
            {
                vOut.add( fileItem.getInputStream( ) );

            }
        }
        catch( IOException e )
        {
            AppLogService.error( "error creating Partial File item sequence inputstream", e );
        }
        _sequenceInputStream = new SequenceInputStream( Collections.enumeration( vOut ) );

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiskFileItem delete( ) throws IOException
    {
        for ( FileItem<DiskFileItem> item : _items )
        {
            item.delete( );
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte [ ] get( )
    {

        byte [ ] bReturn = null;
        try
        {
            bReturn = IOUtils.toByteArray( _sequenceInputStream );
        }
        catch( IOException e )
        {
            AppLogService.error( "error getting Partial File item  inputstream", e );

        }
        return bReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentType( )
    {
        return _items.get( 0 ).getContentType( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFieldName( )
    {
        return _items.get( 0 ).getFieldName( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream( ) throws IOException
    {
        return _sequenceInputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName( )
    {
        return UploadUtil.cleanFileName( FilenameUtils.getName( _items.get( 0 ).getName( ) ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream getOutputStream( ) throws IOException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSize( )
    {
        return _items.stream( ).collect( Collectors.summingLong( FileItem<DiskFileItem>::getSize ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString( )
    {
        return _items.get( 0 ).getString( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString( Charset encoding ) throws IOException
    {
        return _items.get( 0 ).getString( encoding );
    }

    /**
     * Returns the contents of the file item as a String, using the specified
     * encoding.  This method uses {@link #get()} to retrieve the
     * contents of the item.
     * 
     * This method is deprecated. Use getString( Charset encoding ) method instead
     *
     * @param encoding The character encoding to use.
     *
     * @return The contents of the item, as a string.
     *
     * @throws UnsupportedEncodingException if the requested character
     *                                      encoding is not available.
     */
    @Deprecated
    public String getString( String encoding ) throws UnsupportedEncodingException
    {
    	String str = null;
    	try
    	{
    		str = getString( Charset.forName( encoding ) );
    	}
    	catch( IOException e )
    	{
    		throw new UnsupportedEncodingException( e.getMessage( ) );
    	}
        return str;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFormField( )
    {
        return _items.get( 0 ).isFormField( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInMemory( )
    {
        return _items.get( 0 ).isInMemory( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiskFileItem setFieldName( String name )
    {
        return _items.get( 0 ).setFieldName( name );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiskFileItem setFormField( boolean state )
    {
        return _items.get( 0 ).setFormField( state );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiskFileItem write( Path file ) throws IOException
    {
        return null;
    }
    
    /**
     * A convenience method to write an uploaded item to disk. The client code
     * is not concerned with whether or not the item is stored in memory, or on
     * disk in a temporary location. They just want to write the uploaded item
     * to a file.
     * <p>
     * This method is not guaranteed to succeed if called more than once for
     * the same item. This allows a particular implementation to use, for
     * example, file renaming, where possible, rather than copying all of the
     * underlying data, thus gaining a significant performance benefit.
     * 
     * This method is deprecated. Use write( Path file ) method instead
     *
     * @param file The <code>File</code> into which the uploaded item should
     *             be stored.
     *
     * @throws Exception if an error occurs.
     */
    @Deprecated
    public void write( File file ) throws Exception
    {
        // Nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileItemHeaders getHeaders( )
    {
        return _items.get( 0 ).getHeaders( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiskFileItem setHeaders( FileItemHeaders headers )
    {
        // No Default Headers
    	return null;
    }
}
