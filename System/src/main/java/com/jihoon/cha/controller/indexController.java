package com.jihoon.cha.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	
	
	private static int extractMemoryUsage(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length >= 5) {
            String memoryUsage = parts[parts.length - 4];
            try {
                return Integer.parseInt(memoryUsage.replace(",", ""));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
	
	
	@RequestMapping(value = "/processlist")
	@ResponseBody
	public  Map<String, Object>processlist() throws Exception {

	        
	        Map<String, Object> commandMap = new HashMap<>();
			//commandMap.put("data", resultList);
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
		
		String cmd = "wmic logicaldisk get name,size";
		Process p = Runtime.getRuntime().exec("cmd /c " + cmd);

		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String l = null;
		String Result="0";
		
		
		List<Map<String, Object>> resultList = new ArrayList<>();

		while ((l = r.readLine()) != null) {
		    if (!isStringEmpty(l)) {
		        String[] parts = l.split("\\s+");
		        
		        if (parts.length == 2 && !parts[1].equals("Size")) { // Skip lines starting with "Size"
		            String name = parts[0];
		            String size = parts[1];
		            
		            long sizeBytes = Long.parseLong(size);
		            double sizeGigabytes = (double) sizeBytes / (1024 * 1024 * 1024);
		            String sizeFormatted = String.format("%.2f", sizeGigabytes);
		            
		            Map<String, Object> entry = new HashMap<>();
		            entry.put("Size", sizeFormatted);
		            entry.put("Name", name);
		            resultList.add(entry);
		        }
		    }			
		}


		Map<String, Object> commandMap = new HashMap<>();
		commandMap.put("data", resultList);
		return commandMap;
	}
	
	@RequestMapping(value = "/networkinfo")
	@ResponseBody
	public  String networkinfo () throws Exception {
		
		String cmd = "for /f \"tokens=2,3 delims= \" %i in ('netstat -e ^| find \"바이트\"') do @echo %i %j\r\n";
		Process p = Runtime.getRuntime().exec("cmd /c " + cmd);

		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String l = null;
		String Result="0";
		
		
		while ((l = r.readLine()) != null) {
			if (!isStringEmpty(l)) {
				Result=l.toString();
			}			
		}

		 String[] splitData = Result.split(" ");
         long recvBytes = Long.parseLong(splitData[0]);
         long sendBytes = Long.parseLong(splitData[1]);

         double kbpsRecv = (recvBytes * 8) / (1024.0 * 1024.0); 
         double kbpsSend = (sendBytes * 8) / (1024.0 * 1024.0);

         String json = String.format("{\"recv\": %.1f, \"send\": %.1f}", kbpsRecv, kbpsSend);

	
		return json;
	}
	
	
	
	@RequestMapping(value = "/raminfo")
	@ResponseBody
	public  String raminfo () throws Exception {
		
		String cmd = "wmic os get FreePhysicalMemory,TotalVisibleMemorySize";
		Process p = Runtime.getRuntime().exec("cmd /c " + cmd);

		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String l = null;
		String Result="0";
		
		
		while ((l = r.readLine()) != null) {
			if (!isStringEmpty(l)) {
				Result=l.toString();
			}			
		}
		
		String Split_Data[] = Result.split("             ");
		String Use_RAM=Split_Data[0];
		String Value_USERAM=String.format("%.1f",Float.valueOf(Use_RAM)/1024/1024).toString();
		String Total_RAM=Split_Data[1];
		String Value_TOTALRAM=String.format("%.0f",Float.valueOf(Total_RAM)/1024/1024).toString();

		return Value_USERAM +" / "+Value_TOTALRAM;
	}
	
	@RequestMapping(value = "/cpuinfo")
	@ResponseBody
	public  String cpuinfo () throws Exception {
		
		String command = "top -bn 1 | grep 'Cpu(s)'"; // 현재 CPU 사용량 정보 가져오기
		Process process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", command });
		String result="";
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		double cpuUsage = 0.0;

		while ((line = reader.readLine()) != null) {
		    if (line.contains("%Cpu(s)")) {
		        String[] parts = line.split("us,")[0].split(":")[1].trim().split("\\s+");
		        for (String part : parts) {
		            if (part.endsWith("id")) { // id는 idle 시간의 비율
		                cpuUsage = 100.0 - Double.parseDouble(part);
		                result=Double.toString(cpuUsage);
		                break;
		            }
		        }
		        break;
		    }
		}

		reader.close();
		return result;
	}
	
	//index.html 호출
	@GetMapping(value = "/")
	public String index(){
	    return "Index.html";
	}
}
