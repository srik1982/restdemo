package srikarao.rest.testing;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.sun.jersey.core.header.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ejb.Asynchronous;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {
	static EntityTag etag = new EntityTag("xyz");
	static Date lastModified = new Date();
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
	@GET
    @Path("tag")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getIt(@Context Request request) {
    	ResponseBuilder builder = request.evaluatePreconditions(etag);
    	
    	if(builder == null) {
	    	CacheControl cache = new CacheControl();
	    	cache.setNoStore(true);
	    	cache.setPrivate(false);
	    	cache.setMaxAge(300);
	        return Response.ok("Got it!").cacheControl(cache).tag(etag).build();
    	}else {
    		return builder.build();
    	}
    }
    
	@GET
    @Path("modified")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getThat(@Context Request request, @Context HttpServletRequest servletRequest) {
		
		Enumeration<String> _enums = servletRequest.getHeaderNames();
		while(_enums.hasMoreElements()) {
			String elem = _enums.nextElement();
			System.out.println(elem+" : "+servletRequest.getHeader(elem));
		}
		
		ResponseBuilder builder = request.evaluatePreconditions(lastModified);
    	System.out.println("1. builder = "+builder);
		
    	if(builder == null) {
    		System.out.println("2");
	    	CacheControl cache = new CacheControl();
	    	cache.setNoStore(true);
	    	cache.setPrivate(false);
	    	cache.setMaxAge(300);
	        return Response.ok("Got it!").cacheControl(cache).lastModified(lastModified).build();
    	}else {
    		System.out.println("3");
    		return builder.build();
    	}
    }
	
//	@GET
//    @Asynchronous
//    @Path("async")
//    public void asyncRestMethod(@Suspended final AsyncResponse asyncResponse) {
//                String result = heavyLifting();
//                asyncResponse.resume(result);
//     }
//     private String heavyLifting() {
//    	 try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        return "RESULT";
//     }
	
	@GET
	@Path("async")
	@Produces(MediaType.TEXT_PLAIN)
	@Asynchronous
	public void asyncMethod(@Suspended final AsyncResponse asyncResponse) {
		String result = heavyLifting();
		System.out.println(asyncResponse.isDone());
		asyncResponse.resume(Response.ok(result).status(Response.Status.GONE).build());
		System.out.println(asyncResponse.isDone());
	}

	private String heavyLifting() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "My new async method seems to be working";
	}
	
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("upload")
	public Response uploadFile(@FormDataParam("fu") InputStream uploadedInputStream,
							   @FormDataParam("fu") FormDataContentDisposition fileDetails){
		String fileLocation = "e:\\"+fileDetails.getFileName();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fileLocation);
			int read = 0;
			while((read = uploadedInputStream.read())!=-1) {
				fos.write(read);
			}
			fos.flush();
			fos.close();
		}catch(IOException e) {
			e.printStackTrace();
		}finally {
		}
		
		return Response.ok("Upload Successful").status(Response.Status.OK).build();
		
	}
}
