package com.winnovature.utils.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.winnovature.utils.dtos.Templates;

public class JsonUtility {
	
	private final Log logger = LogFactory.getLog(Constants.UtilsLogger);

	public Map<Object, Object> convertJsonToMap(String json) {

		if (logger.isDebugEnabled())
			logger.debug("convertJsonToMap Begin");
		Map<Object, Object> map = new HashMap<Object, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			// convert JSON string to Map
			map = mapper.readValue(json, new TypeReference<Map<Object, Object>>() {});
		} catch (Exception e) {
			logger.error("Exception:", e);
		}
		return map;
	}
	
	public Map<String, Object> jsonToMap(String json) {

		if (logger.isDebugEnabled())
			logger.debug("jsonToMap Begin");
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			// convert JSON string to Map
			map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
		} catch (Exception e) {
			logger.error("Exception:", e);
		}
		return map;
	}
	
	public String mapToJson(Map<String, Object> map) {
		if (logger.isDebugEnabled())
			logger.debug("mapToJson Begin");

		String jsonString = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			// convert Map to json string
			jsonString = mapper.writeValueAsString(map);
		} catch (Exception e) {
			logger.error("Exception:", e);
		}
		return jsonString;
	}
	
	public String convertMapToJson(Map<Object, Object> map) {
		if (logger.isDebugEnabled())
			logger.debug("convertMapToJson Begin");

		String jsonString = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			// convert Map to json string
			jsonString = mapper.writeValueAsString(map);
		} catch (Exception e) {
			logger.error("Exception:", e);
		}
		return jsonString;
	}
	
	public String convertListToJson(List<String> list) {
		if (logger.isDebugEnabled())
			logger.debug("convertListToJson Begin");

		String jsonString = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			// convert list to json string
			jsonString = mapper.writeValueAsString(list);
		} catch (Exception e) {
			logger.error("Exception:", e);
		}
		return jsonString;
	}
	
	
	public List<Map<Object, Object>> convertJsonToListOfMap(String json) {

		if (logger.isDebugEnabled()){
			logger.debug("convertJsonToListOfMap Begin");
		}
			
		List<Map<Object, Object>> listOfMap = new ArrayList<Map<Object, Object>>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			// convert JSON string to Map
			listOfMap = mapper.readValue(json, new TypeReference<List<Map<Object, Object>>>() {});
			if (logger.isDebugEnabled()){
				logger.debug("converted JsonToListOfMap :"+listOfMap);
				logger.debug("convertJsonToListOfMap End.");
			}
		} catch (Exception e) {
			logger.error("Exception:", e);
		}
		return listOfMap;
	}
	
	public String convertListOfMapToJson(List<Map<String, Object>> listOfMap) {
		String json = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			// convert JSON string to Map
			json = mapper.writeValueAsString(listOfMap);
		} catch (Exception e) {
			logger.error("Exception:", e);
		}
		return json;
	}

	@SuppressWarnings("unchecked")
	public Map<String,Object> convertBeanToMap(Object obj){
		if(logger.isDebugEnabled()) {
			logger.debug("convertBeanToMap Begin");
		}
		
		Map<String,Object> userDetail = new HashMap<>();
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			// convert Object to Map
			userDetail = mapper.convertValue(obj, Map.class);
			if (logger.isDebugEnabled()){
				logger.debug("converted BeanToMap :"+userDetail);
				logger.debug("convertBeanToMap End.");
			}
			
		} catch(Exception e) {
			logger.error("Exception: ",e);
		}
		return userDetail;
	}
	
	@SuppressWarnings("unchecked")
	public Map<Object,Object> beanToMap(Object obj){
		if(logger.isDebugEnabled()) {
			logger.debug("convertBeanToMap Begin");
		}
		Map<Object,Object> bean = new HashMap<>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			// convert Object to Map
			bean = mapper.convertValue(obj, Map.class);
			if (logger.isDebugEnabled()){
				logger.debug("converted BeanToMap :"+bean);
			}
		} catch(Exception e) {
			logger.error("Exception: ",e);
		}
		return bean;
	}
	
	public String convertMapToJSON(Map<String, String> jsonString)
			throws Exception {
		
		String json = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			json = mapper.writeValueAsString(jsonString);
		}catch(Exception e) {
			logger.error("Exception: ",e);
		}

		return json;
	}
	
	public Map<String, String> convertJsonStringToMap(String json) throws Exception {

		Map<String, String> map = new HashMap<String, String>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
		} catch (Exception e) {
			logger.error("Exception:", e);
			throw e;
		}
		return map;
	}
	
	
	public Templates convertJsonToTemplate(String jsonString,
			Templates tmplDetails) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		try {
			tmplDetails = mapper.readValue(jsonString, Templates.class);
		} catch (Exception e) {
			logger.error("Exception:", e);
			throw e;
		}
		return tmplDetails;
	}
	
	
}
