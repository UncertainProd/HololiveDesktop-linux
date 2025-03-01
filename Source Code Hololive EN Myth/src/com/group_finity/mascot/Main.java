package com.group_finity.mascot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.image.ImagePairs;
import com.group_finity.mascot.imagesetchooser.ImageSetChooser;
import com.group_finity.mascot.sound.Sounds;
import com.joconner.i18n.Utf8ResourceBundleControl;
import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.nilo.plaf.nimrod.NimRODTheme;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import javax.swing.*;
import java.awt.*;


/**
 * Program entry point.
 *
 * Original Author: Yuki Yamada of Group Finity (http://www.group-finity.com/Shimeji/)
 * Currently developed by HololiveEN Myth Shimeji-ee Group.
 */
public class Main
{
    private static final Logger log = Logger.getLogger( Main.class.getName() );
    // Action that matches the "Gather Around Mouse!" context menu command
    static final String BEHAVIOR_GATHER = "ChaseMouse";

    static
    {
        try
        {
            LogManager.getLogManager( ).readConfiguration( Main.class.getResourceAsStream( "/logging.properties" ) );
        }
        catch( final SecurityException e )
        {
            e.printStackTrace( );
        }
        catch( final IOException e )
        {
            e.printStackTrace( );
        }
        catch( OutOfMemoryError err )
        {
            log.log( Level.SEVERE, "Out of Memory Exception.  There are probably have too many "
                    + "Shimeji mascots in the image folder for your computer to handle.  Select fewer"
                    + " image sets or move some to the img/unused folder and try again.", err );
            Main.showError( "Out of Memory.  There are probably have too many \n"
                    + "Shimeji mascots for your computer to handle.\n"
                    + "Select fewer image sets or move some to the \n"
                    + "img/unused folder and try again." );
            System.exit( 0 );
        }
    }
    private final Manager manager = new Manager( );
    private ArrayList<String> imageSets = new ArrayList<String>( );
    private ConcurrentHashMap<String, Configuration> configurations = new ConcurrentHashMap<String, Configuration>( );
    private ConcurrentHashMap<String, ArrayList<String>> childImageSets = new ConcurrentHashMap<String, ArrayList<String>>( );
    private static Main instance = new Main( );
    private Properties properties = new Properties( );
    private Platform platform;
    private ResourceBundle languageBundle;
    
    private JDialog form;
    
    public static Main getInstance( )
    {
        return instance;
    }
    private static JFrame frame = new javax.swing.JFrame( );

    public static void showError( String message )
    {
        JOptionPane.showMessageDialog( frame, message, "Error", JOptionPane.ERROR_MESSAGE );
    }

    public static void main( final String[] args )
    {
        try
        {
            getInstance( ).run( );
        }
        catch( OutOfMemoryError err )
        {
            log.log( Level.SEVERE, "Out of Memory Exception.  There are probably have too many "
                    + "Shimeji mascots in the image folder for your computer to handle.  Select fewer"
                    + " image sets or move some to the img/unused folder and try again.", err );
            Main.showError( "Out of Memory.  There are probably have too many \n"
                    + "Shimeji mascots for your computer to handle.\n"
                    + "Select fewer image sets or move some to the \n"
                    + "img/unused folder and try again." );
            System.exit( 0 );
        }
    }

    public void run( )
    {   
        // test operating system
        if( !System.getProperty("sun.arch.data.model").equals( "64" ) )
            platform = Platform.x86;
        else
            platform = Platform.x86_64;
        
        // load properties
        properties = new Properties( );
        FileInputStream input;
        try
        {
            input = new FileInputStream( "./conf/settings.properties" );
            properties.load( input );
        }
        catch( FileNotFoundException ex )
        {
        }
        catch( IOException ex )
        {
        }
        
        // load languages
        try   
        {
            ResourceBundle.Control utf8Control = new Utf8ResourceBundleControl( false );
            languageBundle = ResourceBundle.getBundle( "language", Locale.forLanguageTag( properties.getProperty( "Language", "en-GB" ) ), utf8Control );
        }
        catch( Exception ex )
        {
            Main.showError( "The default language file could not be loaded. Ensure that you have the latest shimeji language.properties in your conf directory." );
            exit( );
        }
        
        // load theme
        try
        {
            // default light theme
            NimRODLookAndFeel lookAndFeel = new NimRODLookAndFeel( );
            
            // check for theme properties
            NimRODTheme theme = null;
            try
            {
                if( new File( "./conf/theme.properties" ).isFile( ) )
                {
                    theme = new NimRODTheme( "./conf/theme.properties" );
                }
            }
            catch( Exception exc )
            {
                theme = null;
            }
            
            if( theme == null )
            {
                // default back to light theme if not found/valid
                theme = new NimRODTheme( );
                theme.setPrimary1( Color.decode( "#FFFFFF" ) );
                theme.setPrimary2( Color.decode( "#CCCCCC" ) );
                theme.setPrimary3( Color.decode( "#99CC99" ) );
                theme.setSecondary1( Color.decode( "#999999" ) );
                theme.setSecondary2( Color.decode( "#666666" ) );
                theme.setSecondary3( Color.decode( "#333333" ) );
                theme.setMenuOpacity( 255 );
                theme.setFrameOpacity( 255 );
                    // Set default font size to 13
                java.awt.Font font = theme.getUserTextFont().deriveFont(13f); // 13f ensures the size is set to 13
                theme.setFont(font);
            }
            
            // handle menu size
            if( !properties.containsKey( "MenuDPI" ) )
            {
                properties.setProperty( "MenuDPI", Math.max( java.awt.Toolkit.getDefaultToolkit( ).getScreenResolution( ), 96 ) + "" );
                updateConfigFile( );
            }
            float menuScaling = Float.parseFloat( properties.getProperty( "MenuDPI", "96" ) ) / 96;
            java.awt.Font font = theme.getUserTextFont( ).deriveFont( theme.getUserTextFont( ).getSize( ) * menuScaling );
            theme.setFont( font );
            
            NimRODLookAndFeel.setCurrentTheme( theme );
            JFrame.setDefaultLookAndFeelDecorated( true );
            JDialog.setDefaultLookAndFeelDecorated( true );
            // all done
            lookAndFeel.initialize( );
            UIManager.setLookAndFeel( lookAndFeel );
        }
        catch( Exception ex )
        {
            try
            {
                UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName( ) );
            }
            catch( Exception ex1 )
            {
                log.log( Level.SEVERE, "Look & Feel unsupported.", ex1 );
                exit( );
            }
        }
        
        // Get the image sets to use
        if( !Boolean.parseBoolean( properties.getProperty( "AlwaysShowShimejiChooser", "false" ) ) )
        {
            for( String set : properties.getProperty( "ActiveShimeji", "" ).split( "/" ) )
                if( !set.trim( ).isEmpty( ) )
                    imageSets.add( set.trim( ) );
        }
        if( imageSets.isEmpty( ) )
        {
            imageSets = new ImageSetChooser( frame, true ).display( );
            if( imageSets == null )
            {
                exit( );
            }
        }

        // Load shimejis
        for( int index = 0; index < imageSets.size( ); index++ )
        {
            if( loadConfiguration( imageSets.get( index ) ) == false )
            {
                // failed validation
                configurations.remove( imageSets.get( index ) );
                imageSets.remove( imageSets.get( index ) );
                index--;
            }
        }
        if( imageSets.isEmpty( ) )
        {
            exit( );
        }

        // Create the tray icon
        createTrayIcon( );
        
        // Create the first mascot
        for( String imageSet : imageSets )
        {
            String informationAlreadySeen = properties.getProperty( "InformationDismissed", "" );
            if( configurations.get( imageSet ).containsInformationKey( "SplashImage" ) &&
                ( Boolean.parseBoolean( properties.getProperty( "AlwaysShowInformationScreen", "false" ) ) ||
                  !informationAlreadySeen.contains( imageSet ) ) )
            {
                InformationWindow info = new InformationWindow( );
                info.init( imageSet, configurations.get( imageSet ) );
                info.display( );
                setMascotInformationDismissed( imageSet );
                updateConfigFile( );
            }
            createMascot( imageSet );
        }

        getManager( ).start( );
    }

    private boolean loadConfiguration( final String imageSet )
    {
        try
        {
            // try to load in the correct xml files
            String filePath = "./conf/";
            String actionsFile = filePath + "actions.xml";
            if( new File( filePath + "\u52D5\u4F5C.xml" ).exists( ) )
                actionsFile = filePath + "\u52D5\u4F5C.xml";
            
            final String[] actionsFileNames = {
                "actions.xml",
                "\u52D5\u4F5C.xml",
                "\u00D5\u00EF\u00F2\u00F5\u00A2\u00A3.xml",
                "\u00A6-\u00BA@.xml",
                "\u00F4\u00AB\u00EC\u00FD.xml",
                "one.xml",
                "1.xml",
            };

            filePath = "./conf/" + imageSet + "/";
            for (String filename : actionsFileNames)
            {
                if ( new File(filePath + filename).exists() )
                {
                    actionsFile = filePath + filename;
                    break;
                }
            }
            
            filePath = "./img/" + imageSet + "/conf/";
            for (String filename : actionsFileNames)
            {
                if ( new File(filePath + filename).exists() )
                {
                    actionsFile = filePath + filename;
                    break;
                }
            }

            log.log( Level.INFO, imageSet + " Read Action File ({0})", actionsFile );

            final Document actions = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    new FileInputStream( new File( actionsFile ) ) );
            
            Configuration configuration = new Configuration( );

            configuration.load( new Entry( actions.getDocumentElement( ) ), imageSet );

            filePath = "./conf/";
            String behaviorsFile = filePath + "behaviors.xml";
            if( new File( filePath + "\u884C\u52D5.xml" ).exists( ) )
                behaviorsFile = filePath + "\u884C\u52D5.xml";
            
            final String[] behaviorsFileNames = {
                "behaviors.xml",
                "behavior.xml",
                "\u884C\u52D5.xml",
                "\u00DE\u00ED\u00EE\u00D5\u00EF\u00F2.xml",
                "\u00AA\u00B5\u00A6-.xml",
                "\u00ECs\u00F4\u00AB.xml",
                "two.xml",
                "2.xml",
            };

            filePath = "./conf/" + imageSet + "/";
            for (String filename : behaviorsFileNames)
            {
                if ( new File(filePath + filename).exists() )
                {
                    behaviorsFile = filePath + filename;
                    break;
                }
            }
            
            filePath = "./img/" + imageSet + "/conf/";
            for (String filename : behaviorsFileNames)
            {
                if ( new File(filePath + filename).exists() )
                {
                    behaviorsFile = filePath + filename;
                    break;
                }
            }

            log.log( Level.INFO, imageSet + " Read Behavior File ({0})", behaviorsFile );

            final Document behaviors = DocumentBuilderFactory.newInstance( ).newDocumentBuilder( ).parse( new FileInputStream( new File( behaviorsFile ) ) );

            configuration.load( new Entry( behaviors.getDocumentElement( ) ), imageSet );

            filePath = "./conf/";
            String infoFile = filePath + "info.xml";
            
            filePath = "./conf/" + imageSet + "/";
            if( new File( filePath + "info.xml" ).exists( ) )
                infoFile = filePath + "info.xml";
            
            filePath = "./img/" + imageSet + "/conf/";
            if( new File( filePath + "info.xml" ).exists( ) )
                infoFile = filePath + "info.xml";

            if( new File( infoFile ).exists( ) )
            {
                log.log( Level.INFO, imageSet + " Read Information File ({0})", infoFile );

                final Document information = DocumentBuilderFactory.newInstance( ).newDocumentBuilder( ).parse( new FileInputStream( new File( infoFile ) ) );

                configuration.load( new Entry( information.getDocumentElement( ) ), imageSet );
            }
            
            configuration.validate( );

            configurations.put( imageSet, configuration );
            
            ArrayList<String> childMascots = new ArrayList<String>( );
            
            // born mascot bit goes here...
            for( final Entry list : new Entry( actions.getDocumentElement( ) ).selectChildren( "ActionList" ) )
            {
                for( final Entry node : list.selectChildren( "Action" ) )
                {
                    if( node.getAttributes( ).containsKey( "BornMascot" ) )
                    {
                        String set = node.getAttribute( "BornMascot" );
                        if( !childMascots.contains( set ) )
                            childMascots.add( set );
                        if( !configurations.containsKey( set ) )
                            loadConfiguration( set );
                    }
                    if( node.getAttributes( ).containsKey( "TransformMascot" ) )
                    {
                        String set = node.getAttribute( "TransformMascot" );
                        if( !childMascots.contains( set ) )
                            childMascots.add( set );
                        if( !configurations.containsKey( set ) )
                            loadConfiguration( set );
                    }
                }
            }
            
            childImageSets.put( imageSet, childMascots );

            return true;
        }
        catch( final IOException e )
        {
            log.log( Level.SEVERE, "Failed to load configuration files", e );
            Main.showError( languageBundle.getString( "FailedLoadConfigErrorMessage" ) + "\n" + e.getMessage( ) + "\n" + languageBundle.getString( "SeeLogForDetails" ) );
        }
        catch( final SAXException e )
        {
            log.log( Level.SEVERE, "Failed to load configuration files", e );
            Main.showError( languageBundle.getString( "FailedLoadConfigErrorMessage" ) + "\n" + e.getMessage( ) + "\n" + languageBundle.getString( "SeeLogForDetails" ) );
        }
            catch( final ParserConfigurationException e )
        {
            log.log( Level.SEVERE, "Failed to load configuration files", e );
            Main.showError( languageBundle.getString( "FailedLoadConfigErrorMessage" ) + "\n" + e.getMessage( ) + "\n" + languageBundle.getString( "SeeLogForDetails" ) );
        }
        catch( final ConfigurationException e )
        {
            log.log( Level.SEVERE, "Failed to load configuration files", e );
            Main.showError( languageBundle.getString( "FailedLoadConfigErrorMessage" ) + "\n" + e.getMessage( ) + "\n" + languageBundle.getString( "SeeLogForDetails" ) );
        }
        catch( final Exception e )
        {
            log.log( Level.SEVERE, "Failed to load configuration files", e );
            Main.showError( languageBundle.getString( "FailedLoadConfigErrorMessage" ) + "\n" + e.getMessage( ) + "\n" + languageBundle.getString( "SeeLogForDetails" ) );
        }
        
        return false;
    }

    private BufferedImage trayImg = null;
    private BufferedImage gettrayImg()
    {
        if(trayImg == null)
        {
            return new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        }
        return trayImg;
    }

    private void initPopupForm()
    {

    }

    private void onPopupTriggered()
    {
        
    }

    /**
     * Create a tray icon.
     *
     * @ Throws AWTException
     * @ Throws IOException
     */
    private void createTrayIcon()
    {
        log.log( Level.INFO, "create a tray icon" );
        
        // get the tray icon image
        try
        {
            trayImg = ImageIO.read( Main.class.getResource( "/img/icon.png" ) );
        }
        catch( final Exception e )
        {
            log.log( Level.SEVERE, "Failed to create tray icon", e );
            Main.showError( languageBundle.getString( "FailedDisplaySystemTrayErrorMessage" ) + "\n" + languageBundle.getString( "SeeLogForDetails" ) );
        }

        try
        {
            // create the mouse listener
            MouseListener mlistener = new MouseListener( )
            {
                private int scaling;

                @Override
                public void mouseClicked( MouseEvent event )
                {
                }

                @Override
                public void mousePressed( MouseEvent event )
                {
                    if( event.isPopupTrigger( ) )
                    {
                        // close the form if it's open
                        if( form != null )
                            form.dispose( );
                        
                        // create the form and border
                        form = new JDialog( frame, false );
                        final JPanel panel = new JPanel( );
                        panel.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
                        form.add( panel );
        
                        // buttons and action handling
                        JButton btnCallShimeji = new JButton( languageBundle.getString( "CallShimeji" ) );
                        btnCallShimeji.setText("Summon TALENT");
                        btnCallShimeji.addActionListener( new ActionListener( )
                        {
                            public void actionPerformed( final ActionEvent event )
                            {
                                createMascot( );
                                form.dispose( );
                            }
                        } );

                        JButton btnFollowCursor = new JButton( languageBundle.getString( "FollowCursor" ) );
                        btnFollowCursor.addActionListener( new ActionListener( )
                        {
                            public void actionPerformed( final ActionEvent event )
                            {
                                getManager( ).setBehaviorAll( BEHAVIOR_GATHER );
                                form.dispose( );
                            }
                        } );
                        
                        JButton btnReduceToOne = new JButton( languageBundle.getString( "ReduceToOne" ) );
                        btnReduceToOne.addActionListener( new ActionListener( )
                        {
                            public void actionPerformed( final ActionEvent event )
                            {
                                getManager( ).remainOne( );
                                form.dispose( );
                            }
                        } );
                        
                        JButton btnRestoreWindows = new JButton( languageBundle.getString( "RestoreWindows" ) );
                        btnRestoreWindows.addActionListener( new ActionListener( )
                        {
                            public void actionPerformed( final ActionEvent event )
                            {
                                NativeFactory.getInstance( ).getEnvironment( ).restoreIE( );
                                form.dispose( );
                            }
                        } );
                        
                        final JButton btnAllowedBehaviours = new JButton( languageBundle.getString( "AllowedBehaviours" ) );
                        btnAllowedBehaviours.setText("Myth Allowed Behaviors");
                        btnAllowedBehaviours.addMouseListener( new MouseListener( )
                        {
                            @Override
                            public void mouseClicked( MouseEvent e )
                            {
                            }

                            @Override
                            public void mousePressed( MouseEvent e )
                            {
                            }

                            @Override
                            public void mouseReleased( MouseEvent e )
                            {
                                btnAllowedBehaviours.setEnabled( true );
                            }

                            @Override
                            public void mouseEntered( MouseEvent e )
                            {
                            }

                            @Override
                            public void mouseExited( MouseEvent e )
                            {
                            }
                        } );
                        btnAllowedBehaviours.addActionListener( new ActionListener( )
                        {
                            @Override
                            public void actionPerformed( final ActionEvent event )
                            {
                                // "Disable Breeding" menu item
                                final JCheckBoxMenuItem breedingMenu = new JCheckBoxMenuItem( languageBundle.getString( "BreedingCloning" ), Boolean.parseBoolean( properties.getProperty( "Breeding", "true" ) ) );
                                breedingMenu.setText("Enable Ame Glitch After-Images");
                                breedingMenu.addItemListener( new ItemListener( )
                                {
                                    public void itemStateChanged( final ItemEvent e )
                                    {
                                        breedingMenu.setState( toggleBooleanSetting( "Breeding", true ) );
                                        updateConfigFile( );
                                        btnAllowedBehaviours.setEnabled( true );
                                    }
                                } );

                                final JCheckBoxMenuItem donothingMenu = new JCheckBoxMenuItem( languageBundle.getString("BreedingCloning"), Boolean.parseBoolean(properties.getProperty("JumpEnabled", "true") ) );
                                donothingMenu.setText("Enable AnnoyingGura Mode");
                                donothingMenu.addItemListener(new ItemListener() {
                                    public void itemStateChanged(final ItemEvent e) 
                                    {
                                        donothingMenu.setState(toggleBooleanSetting("JumpEnabled", true) );
                                        updateConfigFile();
                                        btnAllowedBehaviours.setEnabled( true );
                                    }
                                } );

                                
                                // "Disable Breeding Transient" menu item
                                final JCheckBoxMenuItem transientMenu = new JCheckBoxMenuItem( languageBundle.getString( "BreedingTransient" ), Boolean.parseBoolean( properties.getProperty( "Transients", "true" ) ) );
                                transientMenu.addItemListener( new ItemListener( )
                                {
                                    public void itemStateChanged( final ItemEvent e )
                                    {
                                        transientMenu.setState( toggleBooleanSetting( "Transients", true ) );
                                        updateConfigFile( );
                                        btnAllowedBehaviours.setEnabled( true );
                                    }
                                } );
                                
                                // "Disable Transformations" menu item
                                final JCheckBoxMenuItem transformationMenu = new JCheckBoxMenuItem( languageBundle.getString( "Transformation" ), Boolean.parseBoolean( properties.getProperty( "Transformation", "true" ) ) );
                                transformationMenu.addItemListener( new ItemListener( )
                                {
                                    public void itemStateChanged( final ItemEvent e )
                                    {
                                        transformationMenu.setState( toggleBooleanSetting( "Transformation", true ) );
                                        updateConfigFile( );
                                        btnAllowedBehaviours.setEnabled( true );
                                    }
                                } );

                                // "Throwing Windows" menu item
                                final JCheckBoxMenuItem throwingMenu = new JCheckBoxMenuItem( languageBundle.getString( "ThrowingWindows" ), Boolean.parseBoolean( properties.getProperty( "Throwing", "true" ) ) );
                                throwingMenu.addItemListener( new ItemListener( )
                                {
                                    public void itemStateChanged( final ItemEvent e )
                                    {
                                        throwingMenu.setState( toggleBooleanSetting( "Throwing", true ) );
                                        updateConfigFile( );
                                        btnAllowedBehaviours.setEnabled( true );
                                    }
                                } );

                                // "Mute Sounds" menu item
                                final JCheckBoxMenuItem soundsMenu = new JCheckBoxMenuItem( languageBundle.getString( "SoundEffects" ), Boolean.parseBoolean( properties.getProperty( "Sounds", "true" ) ) );
                                soundsMenu.addItemListener( new ItemListener( )
                                {
                                    public void itemStateChanged( final ItemEvent e )
                                    {
                                        boolean result = toggleBooleanSetting( "Sounds", true );
                                        soundsMenu.setState( result );
                                        Sounds.setMuted( !result );
                                        updateConfigFile( );
                                        btnAllowedBehaviours.setEnabled( true );
                                    }
                                } );

                                // "Multiscreen" menu item
                                final JCheckBoxMenuItem multiscreenMenu = new JCheckBoxMenuItem( languageBundle.getString( "Multiscreen" ), Boolean.parseBoolean( properties.getProperty( "Multiscreen", "true" ) ) );
                                multiscreenMenu.addItemListener( new ItemListener( )
                                {
                                    public void itemStateChanged( final ItemEvent e )
                                    {
                                        multiscreenMenu.setState( toggleBooleanSetting( "Multiscreen", true ) );
                                        updateConfigFile( );
                                        btnAllowedBehaviours.setEnabled( true );
                                    }
                                } );
                                
                                JPopupMenu behaviourPopup = new JPopupMenu( );
                                behaviourPopup.add( breedingMenu );
                                behaviourPopup.add( donothingMenu );
                                behaviourPopup.add( transientMenu );
                                behaviourPopup.add( transformationMenu );
                                behaviourPopup.add( throwingMenu );
                                behaviourPopup.add( soundsMenu );
                                behaviourPopup.add( multiscreenMenu );
                                behaviourPopup.addPopupMenuListener( new PopupMenuListener( )
                                {
                                    @Override
                                    public void popupMenuWillBecomeVisible( PopupMenuEvent e )
                                    {
                                    }

                                    @Override
                                    public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
                                    {
                                        if( panel.getMousePosition( ) != null )
                                        {
                                            btnAllowedBehaviours.setEnabled( !( panel.getMousePosition( ).x > btnAllowedBehaviours.getX( ) && 
                                                panel.getMousePosition( ).x < btnAllowedBehaviours.getX( ) + btnAllowedBehaviours.getWidth( ) &&
                                                panel.getMousePosition( ).y > btnAllowedBehaviours.getY( ) && 
                                                panel.getMousePosition( ).y < btnAllowedBehaviours.getY( ) + btnAllowedBehaviours.getHeight( ) ) );
                                        }
                                        else
                                        {
                                            btnAllowedBehaviours.setEnabled( true );
                                        }
                                    }

                                    @Override
                                    public void popupMenuCanceled( PopupMenuEvent e )
                                    {
                                    }
                                } );
                                behaviourPopup.show( btnAllowedBehaviours, 0, btnAllowedBehaviours.getHeight( ) );
                                btnAllowedBehaviours.requestFocusInWindow( );
                            }
                        } );
                        
                        final JButton btnChooseShimeji = new JButton( languageBundle.getString( "ChooseShimeji" ) );
                        btnChooseShimeji.setText("Choose HoloMYTH Member");
                        btnChooseShimeji.addActionListener( new ActionListener( )
                        {
                            public void actionPerformed( final ActionEvent event )
                            {
                                form.dispose( );
                                ImageSetChooser chooser = new ImageSetChooser( frame, true );
                                chooser.setIconImage( gettrayImg() );
                                setActiveImageSets( chooser.display( ) );
                            }
                        } );
                        
                        final JButton btnSettings = new JButton( languageBundle.getString( "Settings" ) );
                        btnSettings.addActionListener( new ActionListener( )
                        {
                            public void actionPerformed( final ActionEvent event )
                            {
                                form.dispose( );
                                SettingsWindow dialog = new SettingsWindow( frame, true );
                                dialog.setIconImage( gettrayImg() );
                                dialog.init( );
                                dialog.display( );
                                
                                if( dialog.getEnvironmentReloadRequired( ) )
                                {
                                    NativeFactory.getInstance( ).getEnvironment( ).dispose( );
                                    NativeFactory.resetInstance( );
                                }
                                if( dialog.getEnvironmentReloadRequired( ) || dialog.getImageReloadRequired( ) )
                                {
                                    // need to reload the shimeji as the images have rescaled
                                    boolean isExit = getManager( ).isExitOnLastRemoved( );
                                    getManager( ).setExitOnLastRemoved( false );
                                    getManager( ).disposeAll( );

                                    // Wipe all loaded data
                                    ImagePairs.clear( );
                                    configurations.clear( );

                                    // Load settings
                                    for( String imageSet : imageSets )
                                    {
                                        loadConfiguration( imageSet );
                                    }

                                    // Create the first mascot
                                    for( String imageSet : imageSets )
                                    {
                                        createMascot( imageSet );
                                    }

                                    Main.this.getManager( ).setExitOnLastRemoved( isExit );
                                }
                                if( dialog.getInteractiveWindowReloadRequired( ) )
                                    NativeFactory.getInstance( ).getEnvironment( ).refreshCache( );
                            }
                        } );
                        
                        final JButton btnLanguage = new JButton( languageBundle.getString( "Language" ) );
                        btnLanguage.addMouseListener( new MouseListener( )
                        {
                            @Override
                            public void mouseClicked( MouseEvent e )
                            {
                            }

                            @Override
                            public void mousePressed( MouseEvent e )
                            {
                            }

                            @Override
                            public void mouseReleased( MouseEvent e )
                            {
                                btnLanguage.setEnabled( true );
                            }

                            @Override
                            public void mouseEntered( MouseEvent e )
                            {
                            }

                            @Override
                            public void mouseExited( MouseEvent e )
                            {
                            }
                        } );
                        btnLanguage.addActionListener( new ActionListener( )
                        {
                            public void actionPerformed( final ActionEvent e )
                            {
                                final String[][] languagePairs = {
                                    {"English", "en-GB"},
                                    {"\u0639\u0631\u0628\u064A", "ar-SA"},
                                    {"Catal\u00E0", "ca-ES"},
                                    {"Deutsch", "de-DE"},
                                    {"Espa\u00F1ol", "es-ES"},
                                    {"Fran\u00E7ais", "fr-FR"},
                                    {"Hrvatski", "hr-HR"},
                                    {"Italiano", "it-IT"},
                                    {"Nederlands", "nl-NL"},
                                    {"Polski", "pl-PL"},
                                    {"Portugu\u00eas Brasileiro", "pt-BR"},
                                    {"Portugu\u00eas", "pt-PT"},
                                    {"\u0440\u0443\u0301\u0441\u0441\u043a\u0438\u0439 \u044f\u0437\u044b\u0301\u043a", "ru-RU"},
                                    {"Rom\u00e2n\u0103", "ro-RO"},
                                    {"Srpski", "sr-RS"},
                                    {"Suomi", "fi-FI"},
                                    {"ti\u1ebfng Vi\u1ec7t", "vi-VN"},
                                    {"\u7b80\u4f53\u4e2d\u6587", "zh-CN"},
                                    {"\u7E41\u9AD4\u4E2D\u6587", "zh-TW"},
                                    {"\ud55c\uad6d\uc5b4", "ko-KR"},
                                    {"\u65E5\u672C\u8A9E", "ja-JP"},
                                };
                                final JMenuItem[] languageMenuItems = new JMenuItem[languagePairs.length];
                                for (int i = 0; i < languagePairs.length; i++)
                                {
                                    final String[] langPair = languagePairs[i];
                                    final String languageName = langPair[0];
                                    final String langCode = langPair[1];
                                    final JMenuItem langMenuItem = new JMenuItem( languageName );
                                    langMenuItem.addActionListener(new ActionListener() {
                                        public void actionPerformed( final ActionEvent e )
                                        {
                                            form.dispose( );
                                            updateLanguage( langCode );
                                            updateConfigFile( );
                                        }
                                    });

                                    languageMenuItems[i] = langMenuItem;
                                }

                                JPopupMenu languagePopup = new JPopupMenu( );
                                boolean shouldAddSeparator = true;
                                for (JMenuItem lang : languageMenuItems)
                                {
                                    languagePopup.add(lang);
                                    if(shouldAddSeparator)
                                    {
                                        languagePopup.addSeparator( );
                                        shouldAddSeparator = false; // to make sure we only do this once at the beginning
                                    }
                                }
                                languagePopup.addPopupMenuListener( new PopupMenuListener( )
                                {
                                    @Override
                                    public void popupMenuWillBecomeVisible( PopupMenuEvent e )
                                    {
                                    }

                                    @Override
                                    public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
                                    {
                                        if( panel.getMousePosition( ) != null )
                                        {
                                            btnLanguage.setEnabled( !( panel.getMousePosition( ).x > btnLanguage.getX( ) && 
                                                panel.getMousePosition( ).x < btnLanguage.getX( ) + btnLanguage.getWidth( ) &&
                                                panel.getMousePosition( ).y > btnLanguage.getY( ) && 
                                                panel.getMousePosition( ).y < btnLanguage.getY( ) + btnLanguage.getHeight( ) ) );
                                        }
                                        else
                                        {
                                            btnLanguage.setEnabled( true );
                                        }
                                    }

                                    @Override
                                    public void popupMenuCanceled( PopupMenuEvent e )
                                    {
                                    }
                                } );
                                languagePopup.show( btnLanguage, 0, btnLanguage.getHeight( ) );
                                btnLanguage.requestFocusInWindow( );
                            }
                        } );
                        
                        JButton btnPauseAll = new JButton( getManager( ).isPaused( ) ? languageBundle.getString( "ResumeAnimations" ) : languageBundle.getString( "PauseAnimations" ) );
                        btnPauseAll.addActionListener( new ActionListener( )
                        {
                            public void actionPerformed( final ActionEvent e )
                            {
                                form.dispose( );
                                getManager( ).togglePauseAll( );
                            }
                        } );

                        
                        JButton btnDismissAll = new JButton( languageBundle.getString( "DismissAll" ) );
                        btnDismissAll.addActionListener( new ActionListener( )
                        {
                            public void actionPerformed( final ActionEvent e )
                            {
                                exit( );
                            }
                        } );
                        
                        // Load the background image
                        ImageIcon backgroundImage = new ImageIcon(properties.getProperty("BackgroundImagePath", "img/BG.png"));
                        int bgPanelWidth = Integer.parseInt(properties.getProperty("RightClickDialogWidth", ""+backgroundImage.getIconWidth()));
                        int bgPanelHeight = Integer.parseInt(properties.getProperty("RightClickDialogHeight", ""+backgroundImage.getIconHeight()));
                        
                        // Create a custom JPanel to draw the background image
                        JPanel backgroundPanel = new JPanel() {
                            @Override
                            protected void paintComponent(Graphics g) {
                                super.paintComponent(g);
                                Graphics2D g2d = (Graphics2D) g.create();
                                float opacity = 0.9f; // Set the desired opacity (0.0f to 1.0f)
                                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                                g2d.drawImage(backgroundImage.getImage(), 0, 0, bgPanelWidth, bgPanelHeight, this);
                                g2d.dispose();
                            }
                        };
                        backgroundPanel.setBounds(0, 0, bgPanelWidth, bgPanelHeight); // Set the size of the background panel
                        
                        // Create a JLayeredPane
                        JLayeredPane layeredPane = new JLayeredPane();
                        layeredPane.setPreferredSize(new Dimension(bgPanelWidth, bgPanelHeight));
                        
                        // Add the background panel to the lowest layer
                        layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);

                        JPanel buttonPanel = new JPanel();
                        buttonPanel.setOpaque(false);
                        buttonPanel.setPreferredSize(new Dimension(50, 50));
                        
                        // Use the existing panel variable and set its layout
                        panel.setLayout(new GridBagLayout());
                        panel.setOpaque(false); // Make the panel transparent
                        GridBagConstraints gridBag = new GridBagConstraints();
                        gridBag.fill = GridBagConstraints.HORIZONTAL;
                        gridBag.gridx = 0;
                        gridBag.gridy = 0;

                        final JButton buttons[] = {
                            btnCallShimeji,
                            btnFollowCursor,
                            btnReduceToOne,
                            btnRestoreWindows,
                            btnAllowedBehaviours,
                            btnChooseShimeji,
                            btnSettings,
                            btnLanguage,
                            btnPauseAll,
                            btnDismissAll,
                        };
                    
                        // Add buttons to the panel
                        gridBag.insets = new Insets((int) (5 * scaling), 0, 5, 0);
                        gridBag.gridy++;
                        for (JButton btn : buttons)
                        {
                            panel.add(btn, gridBag);
                            gridBag.gridy++;
                        }
                        panel.add(new JSeparator(), gridBag);
                        panel.add(buttonPanel, gridBag);
                        gridBag.gridy++;
                        panel.add(buttonPanel, gridBag);
                        gridBag.gridy++;
                        

                        // Add the panel to the JLayeredPane at a higher layer
                        panel.setBounds(0, 0, bgPanelWidth, bgPanelHeight);
                        layeredPane.add(panel, JLayeredPane.PALETTE_LAYER);
                        
                        
                        // Add the JLayeredPane to the JFrame
                        form.add(layeredPane);
                        form.pack();
                        form.setVisible(true);
                        
                        // Your existing code
                        form.setIconImage(gettrayImg());
                        form.setTitle(languageBundle.getString("Hololive"));
                        form.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                        form.setAlwaysOnTop(true);
                        
                        // Set the form dimensions
                        java.awt.FontMetrics metrics = btnCallShimeji.getFontMetrics(btnCallShimeji.getFont());
                        int width = -1;
                        
                        for (JButton btn : buttons) {
                            width = Math.max(metrics.stringWidth(btn.getText()), width);
                        }
                        panel.setPreferredSize(new Dimension(width + 64,
                                (int) (24 * scaling) + // 12 padding on top and bottom
                                (int) (75 * scaling) + // 13 insets of 5 height normally
                                10 * metrics.getHeight() + // 10 button faces
                                84));
                        form.pack();
                        
                        // Setting location of the form
                        form.setLocation(event.getPoint().x - form.getWidth(), event.getPoint().y - form.getHeight());

                        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                        if (form.getX() < screen.getX()) {
                            form.setLocation(event.getPoint().x, form.getY());
                        }
                        if (form.getY() < screen.getY()) {
                            form.setLocation(form.getX(), event.getPoint().y);
                        }
                        form.setVisible(true);
                        form.setMinimumSize(form.getSize());
                        
                        // Ensure the form is visible and has a minimum size
                        form.setVisible(true);
                        form.setMinimumSize(form.getSize());
                    }
                }

                @Override
                public void mouseReleased( MouseEvent event )
                {
                    if( event.getButton( ) == MouseEvent.BUTTON1 )
                    {
                        createMascot( );
                    }
                    else if( event.getButton( ) == MouseEvent.BUTTON2 && event.getClickCount( ) == 2 )
                    {
                        if( getManager( ).isExitOnLastRemoved( ) )
                        {
                            getManager( ).setExitOnLastRemoved( false );
                            getManager( ).disposeAll( );
                        }
                        else
                        {
                            for( String imageSet : imageSets )
                            {
                                createMascot( imageSet );
                            }
                            getManager( ).setExitOnLastRemoved( true );
                        }
                    }
                }

                @Override
                public void mouseEntered( MouseEvent e )
                {
                }

                @Override
                public void mouseExited( MouseEvent e )
                {
                }
            };

            if (!SystemTray.isSupported())
            {
                // return;
                // Using a window which you can right click is kinda hack-y. gotta find a better way
                log.log(Level.WARNING, "This system does not support system tray!");
                Frame win = new Frame("Tray icon substitute");
                Label label = new Label("Right click this window to get the desktop-pet settings dialog");
                label.setAlignment(Label.CENTER);
                win.add(label);
                win.setSize(420, 185);
                win.setVisible(true);
                win.setIconImage(gettrayImg());
                win.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e)
                    {
                        System.exit(0);
                    }
                });
                System.out.println("Adding mouse listener");
                win.addMouseListener(mlistener);
                label.addMouseListener(mlistener);
                return;
            }

            // Create the tray icon
            final TrayIcon icon = new TrayIcon( gettrayImg(), languageBundle.getString( "ShimejiEE" ) );
            
            // attach menu
            icon.addMouseListener( mlistener );
            // Show tray icon
            SystemTray.getSystemTray( ).add( icon );
        }
        catch( final AWTException e )
        {
            log.log( Level.SEVERE, "Failed to create tray icon", e );
            Main.showError( languageBundle.getString( "FailedDisplaySystemTrayErrorMessage" ) + "\n" + languageBundle.getString( "SeeLogForDetails" ) );
            exit( );
        }
    }

    // Randomly creates a mascot
    public void createMascot( )
    {
        int length = imageSets.size( );
        int random = ( int ) ( length * Math.random( ) );
        createMascot( imageSets.get( random ) );
    }

    /**
     * Create a mascot
     */
    public void createMascot( String imageSet )
    {
        log.log( Level.INFO, "create a mascot" );

        // Create one mascot
        final Mascot mascot = new Mascot( imageSet );

        // Create it outside the bounds of the screen
        mascot.setAnchor( new Point( -4000, -4000 ) );

        // Randomize the initial orientation
        mascot.setLookRight( Math.random( ) < 0.5 );

        try
        {
            mascot.setBehavior( getConfiguration( imageSet ).buildNextBehavior( null, mascot ) );
            this.getManager().add( mascot );
        }
        catch( final BehaviorInstantiationException e )
        {
            log.log( Level.SEVERE, "Failed to initialize the first action", e );
            Main.showError( languageBundle.getString( "FailedInitialiseFirstActionErrorMessage" ) + "\n" + e.getMessage( ) + "\n" + languageBundle.getString( "SeeLogForDetails" ) );
            mascot.dispose();
        }
        catch( final CantBeAliveException e )
        {
            log.log( Level.SEVERE, "Fatal Error", e );
            Main.showError( languageBundle.getString( "FailedInitialiseFirstActionErrorMessage" ) + "\n" + e.getMessage( ) + "\n" + languageBundle.getString( "SeeLogForDetails" ) );
            mascot.dispose();
        }
        catch( Exception e )
        {
            log.log( Level.SEVERE, imageSet + " fatal error, can not be started.", e );
            Main.showError( languageBundle.getString( "CouldNotCreateShimejiErrorMessage" ) + " " + imageSet + ".\n" + e.getMessage( ) + "\n" + languageBundle.getString( "SeeLogForDetails" ) );
            mascot.dispose();
        }
    }
    
    private void refreshLanguage( )
    {
        ResourceBundle.Control utf8Control = new Utf8ResourceBundleControl( false );
        languageBundle = ResourceBundle.getBundle( "language", Locale.forLanguageTag( properties.getProperty( "Language", "en-GB" ) ), utf8Control );

        boolean isExit = getManager( ).isExitOnLastRemoved( );
        getManager( ).setExitOnLastRemoved( false );
        getManager( ).disposeAll( );

        // Load settings
        for( String imageSet : imageSets )
        {
            loadConfiguration( imageSet );
        }

        // Create the first mascot
        for( String imageSet : imageSets )
        {
            createMascot( imageSet );
        }

        getManager( ).setExitOnLastRemoved( isExit );
    }
    
    private void updateLanguage( String language )
    {
        if( !properties.getProperty( "Language", "en-GB" ).equals( language ) )
        {
            properties.setProperty( "Language", language );
            refreshLanguage( );
        }        
    }
    
    private boolean toggleBooleanSetting( String propertyName, boolean defaultValue )
    {
        if( Boolean.parseBoolean( properties.getProperty( propertyName, defaultValue + "" ) ) )
        {
            properties.setProperty( propertyName, "false" );
            return false;
        }
        else
        {
            properties.setProperty( propertyName, "true" );
            return true;
        }
    }
    
    private void setMascotInformationDismissed( final String imageSet )
    {
        ArrayList<String> list = new ArrayList<String>( );
        String[ ] data = properties.getProperty( "InformationDismissed", "" ).split( "/" );
        
        if( data.length > 0 && !data[ 0 ].equals( "" ) )
            list.addAll( Arrays.asList( data ) );
        if( !list.contains( imageSet ) )
            list.add( imageSet );
        
        properties.setProperty( "InformationDismissed", list.toString( ).replace( "[", "" ).replace( "]", "" ).replace( ", ", "/" ) );
    }
    
    public void setMascotBehaviorEnabled( final String name, final Mascot mascot, boolean enabled )
    {
        ArrayList<String> list = new ArrayList<String>( );
        String[ ] data = properties.getProperty( "DisabledBehaviours." + mascot.getImageSet( ), "" ).split( "/" );
        
        if( data.length > 0 && !data[ 0 ].equals( "" ) )
            list.addAll( Arrays.asList( data ) );
        
        if( list.contains( name ) && enabled )
            list.remove( name );
        else if( !list.contains( name ) && !enabled )
            list.add( name );
        
        if( list.size( ) > 0 )
            properties.setProperty( "DisabledBehaviours." + mascot.getImageSet( ), list.toString( ).replace( "[", "" ).replace( "]", "" ).replace( ", ", "/" ) );
        else
            properties.remove( "DisabledBehaviours." + mascot.getImageSet( ) );
        
        updateConfigFile( );
    }
    
    private void updateConfigFile( )
    {
        try
        {
            FileOutputStream output = new FileOutputStream( "./conf/settings.properties" );
            try
            {
                properties.store( output, "HololiveEN Myth Shimeji-ee Configuration Options" );
            }
            finally
            {
                output.close( );
            }
        }
        catch( Exception unimportant )
        {
        }
    }
    
    /** 
     * Replaces the current set of active imageSets without modifying
     * valid imageSets that are already active. Does nothing if newImageSets 
     * are null
     *
     * @param newImageSets All the imageSets that should now be active
     * @author snek, with some tweaks by Kilkakon
     * */
    private void setActiveImageSets( ArrayList<String> newImageSets )
    {
        if( newImageSets == null )
            return;

        // I don't think there would be enough imageSets chosen at any given
        // time for it to be worth using HashSet but i might be wrong
        ArrayList<String> toRemove = new ArrayList<String>( imageSets );
        toRemove.removeAll( newImageSets );

        ArrayList<String> toAdd = new ArrayList<String>( );
        ArrayList<String> toRetain = new ArrayList<String>( );
        for( String set : newImageSets )
        {
            if( !imageSets.contains( set ) )
                toAdd.add( set );
            if( !toRetain.contains( set ) )
                toRetain.add( set );
            populateArrayListWithChildSets( set, toRetain );
        }
        
        boolean isExit = Main.this.getManager( ).isExitOnLastRemoved( );
        Main.this.getManager( ).setExitOnLastRemoved( false );

        for( String r : toRemove )
            removeLoadedImageSet( r, toRetain );
        
        for( String a : toAdd )
            addImageSet( a );
        
        Main.this.getManager( ).setExitOnLastRemoved( isExit );
    }

    private void populateArrayListWithChildSets( String imageSet, ArrayList<String> childList )
    {
        if( childImageSets.containsKey( imageSet ) )
        {
            for( String set : childImageSets.get( imageSet ) )
            {
                if( !childList.contains( set ) )
                {
                    populateArrayListWithChildSets( set, childList );
                    childList.add( set );
                }
            }
        }
    }

    private void removeLoadedImageSet( String imageSet, ArrayList<String> setsToIgnore )
    {
        if( childImageSets.containsKey( imageSet ) )
        {
            for( String set : childImageSets.get( imageSet ) )
            {
                if( !setsToIgnore.contains( set ) )
                {
                    setsToIgnore.add( set );
                    imageSets.remove( imageSet );
                    getManager( ).remainNone( imageSet );
                    configurations.remove( imageSet );
                    ImagePairs.removeAll( imageSet );
                    removeLoadedImageSet( set, setsToIgnore );
                }
            }
        }
        
        if( !setsToIgnore.contains( imageSet ) )
        {
            imageSets.remove( imageSet );
            getManager( ).remainNone( imageSet );
            configurations.remove( imageSet );
            ImagePairs.removeAll( imageSet );
        }
    }
    
    private void addImageSet( String imageSet )
    {
        if( configurations.containsKey( imageSet ) )
        {
            imageSets.add( imageSet );
            createMascot( imageSet );
        }
        else
        {
            if( loadConfiguration( imageSet ) )
            {
                imageSets.add( imageSet );
                String informationAlreadySeen = properties.getProperty( "InformationDismissed", "" );
                if( configurations.get( imageSet ).containsInformationKey( "SplashImage" ) &&
                    ( Boolean.parseBoolean( properties.getProperty( "AlwaysShowInformationScreen", "false" ) ) ||
                      !informationAlreadySeen.contains( imageSet ) ) )
                {
                    InformationWindow info = new InformationWindow( );
                    info.init( imageSet, configurations.get( imageSet ) );
                    info.display( );
                    setMascotInformationDismissed( imageSet );
                    updateConfigFile( );
                }
                createMascot( imageSet );
            }
            else
            {
                // conf failed
                configurations.remove( imageSet ); // maybe move this to the loadConfig catch
            }
        }
    }

    public Configuration getConfiguration( String imageSet )
    {
        return configurations.get( imageSet );
    }

    private Manager getManager( )
    {
        return this.manager;
    }
        
    public Platform getPlatform( )
    {
        return platform;
    }

    public Properties getProperties( )
    {
        return properties;
    }

    public ResourceBundle getLanguageBundle( )
    {
        return languageBundle;
    }

    public void exit( )
    {
        this.getManager( ).disposeAll( );
        this.getManager( ).stop( );
        System.exit( 0 );
    }
}
