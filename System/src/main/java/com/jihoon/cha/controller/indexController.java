package com.jihoon.cha.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.jihoon.cha.mapper.indexMapper;
import ch.qos.logback.core.model.Model;



@Controller
public class indexController {

	@Autowired
	indexMapper indexmapper;
	
	static boolean isStringEmpty(String str) {
        return str == null || str.isEmpty();
    }
	
	private static String formatMemory(long memoryInGB) {
        return String.format("%.0f", (double) memoryInGB);
    }
	

	
	@RequestMapping(value = "/processlist")
	@ResponseBody
	public  Map<String, Object>processlist() throws Exception {

		List<Map<String, Object>> resultList = new ArrayList<>();
		
		Set<String> seenCommands = new HashSet<>();
		 ProcessHandle.allProcesses()
         .sorted(Comparator.comparingLong(ProcessHandle::pid))
         .filter(info -> info.info().startInstant().isPresent())
         .filter(info -> seenCommands.add(info.info().command().orElse("")))
         .sorted(Comparator.comparing(info -> info.info().startInstant().get(), Comparator.reverseOrder()))
         .distinct() 
         .limit(5)
         .forEach(processHandle -> {
        	 String[] pathp=processHandle.info().command().get().split("\\\\");
        	 String fileName = pathp[pathp.length - 1];
             
             Map<String, Object> entry = new HashMap<>();
             entry.put("pid", processHandle.pid() );
             entry.put("exe", fileName);
             resultList.add(entry);
             
         });
	        
	        Map<String, Object> commandMap = new HashMap<>();
			commandMap.put("data", resultList);
			return commandMap;
	}
	
	
	@RequestMapping(value = "/dbvar")
	@ResponseBody
	public  Map<String, Object>dbvar (Model model, @RequestParam Map<String, Object> commandMap) throws Exception {
		List<Map<String, Object>> dbvar = indexmapper.dbvar(commandMap);
		commandMap.put("data", dbvar);
		return commandMap;
	}
	
	
	@RequestMapping(value = "/hddlist")
	@ResponseBody
	public  Map<String, Object>hddlist () throws Exception {
		
	
		Path path = FileSystems.getDefault().getPath("/");
		
		FileStore fileStore = Files.getFileStore(path);
        
        long totalSpace = fileStore.getTotalSpace();
        long usableSpace = fileStore.getUsableSpace();
        long usedSpace = totalSpace - usableSpace;
        
        double totalSpaceGB = totalSpace / (1024.0 * 1024 * 1024);
        double usableSpaceGB = usableSpace / (1024.0 * 1024 * 1024);
        double usedSpaceGB = usedSpace / (1024.0 * 1024 * 1024);
        
		List<Map<String, Object>> resultList = new ArrayList<>();
		
		
        Map<String, Object> entry = new HashMap<>();
        entry.put("Size", totalSpaceGB);
        entry.put("Name", "전체용량");
        resultList.add(entry);
        
        entry = new HashMap<>();
        entry.put("Size", usedSpaceGB);
        entry.put("Name", "사용량");
        resultList.add(entry);
        
        
        entry = new HashMap<>();
        entry.put("Size", usableSpaceGB);
        entry.put("Name", "남은용량");
        resultList.add(entry);
        



		Map<String, Object> commandMap = new HashMap<>();
		commandMap.put("data", resultList);
		return commandMap;
	}
	
	@RequestMapping(value = "/networkinfo")
	@ResponseBody
	public  String networkinfo () throws Exception {
		
			String json="";
		  	String serverAddress = "app.cloudtype.io";
	        int serverPort = 443;
	        int bufferSize = 1024; // 업로드 및 다운로드할 데이터의 크기

	        try (Socket socket = new Socket(serverAddress, serverPort);
	             OutputStream out = socket.getOutputStream();
	             InputStream in = socket.getInputStream()) {

	            // 업로드 속도 체크
	            byte[] uploadData = new byte[bufferSize];
	            long uploadStartTime = System.nanoTime();

	            out.write(uploadData);

	            long uploadEndTime = System.nanoTime();
	            double uploadElapsedTimeInSeconds = (uploadEndTime - uploadStartTime) / 1e9;
	            double uploadSpeedKbps = (bufferSize * 8.0 / uploadElapsedTimeInSeconds) / 1000000;

	            // 다운로드 속도 체크
	            byte[] downloadData = new byte[bufferSize];
	            long downloadStartTime = System.nanoTime();

	            int bytesRead;
	            while ((bytesRead = in.read(downloadData)) != -1) {
	            }

	            long downloadEndTime = System.nanoTime();
	            double downloadElapsedTimeInSeconds = (downloadEndTime - downloadStartTime) / 1e9;
	            double downloadSpeedKbps = (bufferSize * 8.0 / downloadElapsedTimeInSeconds) / 1000000;


	             json = String.format("{\"recv\": %.1f, \"send\": %.1f}",downloadSpeedKbps, uploadSpeedKbps);

	        } catch (Exception e){
	            
	        }

	        

	
		return json;
	}
	
	
	
	@RequestMapping(value = "/raminfo")
	@ResponseBody
	public  String raminfo () throws Exception {
		OperatingSystemMXBean osbean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
		long freeMemory = ((com.sun.management.OperatingSystemMXBean) osbean).getFreePhysicalMemorySize() / 1024 / 1024;
        long totalMemory = ((com.sun.management.OperatingSystemMXBean) osbean).getTotalPhysicalMemorySize() / 1024 / 1024 ;
        String memoryInfo = formatMemory(freeMemory) + "/" + formatMemory(totalMemory);
		return memoryInfo;
	}
	
	@RequestMapping(value = "/cpuinfo")
	@ResponseBody
	public  String cpuinfo () throws Exception {
		
		OperatingSystemMXBean osbean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
		String cpuUsage = String.format("%.2f", ((com.sun.management.OperatingSystemMXBean) osbean).getSystemCpuLoad() * 100);
		
		return cpuUsage;
	}
	
	//index.html 호출
	@GetMapping(value = "/")
	public String index(){
	    return "Index.html";
	}
}
