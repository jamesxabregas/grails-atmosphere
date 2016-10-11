## Grails Atmosphere plugin ##

This page describes the features of the [Grails Atmosphere plugin](http://grails.org/plugin/atmosphere/) that integrates the [Atmosphere](https://atmosphere.dev.java.net/) framework in a Grails application.

### Installation ###

The Grails Atmosphere plugin is installed using the Grails command `install-plugin`:

    grails install-plugin atmosphere

This command will install the latest version of the plugin, version 2.1.6, which includes a Grails runtime dependency (that can be overwritten by the application that uses the plugin) to the Atmosphere's runtime module, version 2.1.6.


### Description ###

The Grails Atmosphere plugin uses the Atmosphere runtime module, and includes the following features:

* the inclusion of Atmosphere framework libraries necessaries to write Atmosphere handlers
* automatic generation of Atmosphere configuration artifacts
* injection of the dynamic method getBroadcaster in Grails controllers and services (see below)
* a GSP tag to include JavaScript files coming from the Atmosphere jQuery plugin
* the plugin has been updated to be compatible with both the [Resources](http://grails.org/plugin/resources) and [Asset Pipeline](http://grails.org/plugin/asset-pipeline) plugins

### Creating Atmosphere handlers ###

The Grails Atmosphere plugin offers two different ways to create a //handler// in a Grails application: the first one is to create a Java or Groovy class that implements the `AtmosphereHandler` interface, and declare it in the `AtmosphereConfig.groovy` file; the second one, the most easy, is to create a Grails service in the application that will be associated with an Atmosphere handler that will be automatically created.
There is a third way to proceed: by accessing the Atmosphere servlet, it is possible to create or remove Atmosphere handlers dynamically.

#### Creating an Atmosphere handler with the command `create-atmosphere-handler` ####

Creating an Atmosphere handler by the Grails command `create-atmosphere-handler` in an application Grails, is realized in two steps, described below.

1. Create a handler class that implements the `AtmosphereHandler` interface

To add an Atmosphere handler to your application, you can use the `create-atmosphere-handler` command coming with the Grails plugin:

    grails create-atmosphere-handler [a.package.handler.name]

If you don't enter a name for the handler, qualified or not, the command will ask for one; it has the effect of creating a new Groovy class in the directory `grails-app/atmosphereHandlers`. For example, using `a.package.chat` as name handler, the class `ChatAtmosphereHandler` will be created:

    package a.package
    
    import org.atmosphere.cpr.AtmosphereHandler
    import org.atmosphere.cpr.AtmosphereResource
    import org.atmosphere.cpr.AtmosphereResourceEvent
    import javax.servlet.http.HttpServletRequest
    import javax.servlet.http.HttpServletResponse
    
    
    class ChatAtmosphereHandler implements AtmosphereHandler <HttpServletRequest, HttpServletResponse> {
    
        void onRequest(AtmosphereResource<HttpServletRequest, HttpServletResponse> event) throws IOException {
    
        }
    
        void onStateChange (AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) throws IOException {
        }
        
        void destroy(){
        }
    
    }

Using the `create-atmosphere-handler` is not required! Just create a Groovy or Java class that implements the `AtmosphereHandler` interface.

2. Next, declare the new handler in the file `AtmosphereConfig.groovy`

Located in the `grails-app/conf` directory, the configuration file `AtmosphereConfig.groovy` can define parameters for integration with Atmosphere and handlers to be used in the Grails application.
For the new handler will be taken into account, add a new call `atmosphere-handler` in the Closure `atmosphereDotXml`.
So be careful to indicate clearly the class of the handler in the parameter `class-name`, and the parameters of the `atmosphere-handler` call are simply attributes of the XML element `atmosphere-handler` coming from the Atmosphere file `atmosphere.xml`. For a more detailed explanation, see the presentation of the `AtmosphereConfig.groovy` file further.


#### Creating an Atmosphere handler through a Grails service ####

Creating an Atmosphere handler through a Grails service is the easiest way to integrate Atmosphere in your application, in which case it's not necessary to declare the handler in the configuration file `AtmosphereConfig.groovy`.
Simply add the static property `atmosphere` to an existing Grails service, and the Closures `onRequest` and `onStateChange`, as in the example below:

    class MagneticPoetryService {
    
        static transactional = false
    
        static atmosphere = [mapping: '/atmosphere/magneticPoetry']
                                            
        def onRequest = { event ->
        }
    
        def onStateChange = { event ->
        }
    
    }

For the service to be associated with a handler, the static `atmosphere` field must be a Map containing the key mapping and specifying the path to the servlet Atmosphere (the equivalent of what one would specify for context-root in the `atmosphere.xml` file).

When you start the web application, the plugin will create for a such service, an Atmosphere handler, responsible for invoking the `onRequest` and `onStateChange` Closures that correspond to the methods of the same name in the `AtmosphereHandler` interface.
Note that the Grails service must be of singleton type, which is the default.


#### Creating (or removing) an Atmosphere handler using the Atmosphere servlet ####

The Atmosphere servlet has an API to add or remove a handler dynamically; it is an interesting approach because you can choose at run-time the mapping URI you want to associate to a handler when creating it.
You access to the Atmosphere servlet (an instance of the class `com.odelia.grails.plugins.atmosphere.StratosphereServlet` that extends `org.atmosphere.cpr.AtmosphereServlet`) from a Grails controller with code like this:

    def servlet = servletContext[com.odelia.grails.plugins.atmosphere.StratosphereServlet.ATMOSPHERE_PLUGIN_ATMOSPHERE_SERVLET]

So, using the reference, you can call methods like `addAtmosphereHandler` or `removeAtmosphereHandler`.


#### The dynamic method `getBroadcaster` ####

The Grails Atmosphere plugin injects a dynamic method, `getBroadcaster`, in all Grails controllers and all services with the static `atmosphere` field described above.
This method returns a map whose entries consist of pairs //mapping/broadcaster//, ie from an Atmosphere mapping, you get the default object `Broadcaster` associated with it in order to broadcast messages.

In a Grails controller or service with the method `getBroadcaster`, you can post a message like this: 

    broadcaster['/atmosphere/magneticPoetry'].broadcast('Hello world!')


#### The resources tag ####

Since its 0.3.1, the Grails Atmosphere plugin defines the GSP tag `atmosphere:resources`; this tag permits to include JavaScript files coming from the Atmosphere jQuery plugin, in a GSP page.
So, in a .gsp view you can use it like this: 

    <atmosphere:resources />

If for some raison, you don't want include the jQuery library used by the Atmosphere jQuery plugin, and just include the `jquery.atmosphere.js` file, you can do it with:

    <g:javascript plugin="atmosphere" src="jquery.atmosphere.js" />

#### Limitations ####

Currently, the Grails Atmosphere plugin does not take into account the modification of a Grails service during the execution of the application (in development mode): if you edit a such service, it is advisable to stop and then restart the web application. 


### Atmosphere Artifacts ###


#### Artifacts created during the installation of the Grails plugin ####

**AtmosphereConfig.groovy**

During installation, the plugin creates the Atmosphere configuration file `AtmosphereConfig.groovy` in the `conf` directory of your Grails application. Of the same nature as the configuration file `Config.groovy`, `AtmosphereConfig.groovy` file contains all the Atmosphere configuration information:  the definition of Atmosphere handlers, and some general configuration settings.

Here is the original contents of this file:

    atmospherePlugin {

        servlet {
        
            // Servlet initialization parameters
            // Example: initParams = ['org.atmosphere.useNative': 'true', 'org.atmosphere.useStream': 'false']
            initParams = []
            urlPattern = '/atmosphere/*'
        }
        
        handlers {
            // This closure is used to generate the atmosphere.xml in META-INF folder, using a MarkupBuilder instance
            atmosphereDotXml = {
                //'atmosphere-handler'('context-root': '/atmosphere/chat', 'class-name': 'com.odelia.grails.plugins.atmosphere.ChatAtmosphereHandler')
        }   
    }


The Closure `atmosphereDotXml` defines Atmosphere handlers used in your Grails application, in addition to those defined through Grails services: this is used when compiling the application to generate the `atmosphere.xml` file with element document `atmosphere-handlers`; to build the XML subelements `atmosphere-handler`, the plugin assigns the execution of the Closure `atmosphereDotXml` to an instance of the Groovy class `MarkupBuilder`, and can generate the document `atmosphere.xml`.
Thus the document `atmosphere.xml`, corresponding to the original configuration `AtmosphereConfig.groovy`, and having uncommented the call `'atmosphere-handler'()`, will include:

    <atmosphere-handlers>
      <atmosphere-handler context-root='/atmosphere/chat' class-name='com.odelia.grails.plugins.atmosphere.ChatAtmosphereHandler' />
    </atmosphere-handlers>

The other configuration elements found in `atmospherePlugin.servlet` are used to define the parameters of the `AtmosphereServlet` servlet (actually a derived class), when the plugin participate to the building of the `web.xml` file: we find the definition of initialization parameters of the servlet and its URL mapping.

**context.xml**

The `context.xml` file with this content:

    <Context>
        <Loader delegate="true"/>
    </Context>

is created in the `WEB-INF` directory.


#### Artifacts created during the compilation of the Grails application ####


**atmosphere.xml**

The Grails Atmosphere plugin generates or regenerates the `atmosphere.xml` file in the `META-INF` folder, according Atmosphere handlers defined in the configuration file `AtmosphereConfig.groovy`.

**atmosphere-decorators.xml**

A file with the name `atmosphere-decorators.xml` is created in the `WEB-INF` directory of the Grails application, and the Grails application file `sitemesh.xml` located in this directory is modified to take account of the created file.

The `atmosphere-decorators.xml` file contains something like:

    <decorators>
        <excludes>
            <pattern>/atmosphere/*</pattern>
        </excludes>
    </decorators>


where the value of the `pattern` element is replaced by one defined in the Groovy configuration file `AtmosphereConfig.groovy` for `atmospherePlugin.servlet.urlPattern`.

The `sitemesh.xml` file is changed by adding this XML element:

    <excludes file="/WEB-INF/atmosphere-decorators.xml"/>

This change solves the errors that can occur with the use of the Jetty web container.
