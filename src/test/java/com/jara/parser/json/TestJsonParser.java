package com.jara.parser.json;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;

import com.jara.util.ParseUtil;


public class TestJsonParser {
	
	private KieBase kieBase = null;
	
	@Test
	public void testParserModelTree() throws Exception{
		//String json = "{\"riesgos\": [ { \"Persona\":{ \"name\":\"mkyong\", \"id\":1, \"att\":null}, \"Persona2\":{ \"name2\":\"mkyong\", \"id2\":1, \"att2\":null}  } ] }";
        // COMPILACION DROOLS
		KieServices ks = KieServices.Factory.get();
        KieContainer kContainer = ks.getKieClasspathContainer();
        
        kieBase = kContainer.getKieBase();
        // PARSER JSON NEGOCIO VS DECLARE TYPES [com.sura.seguros.core.modelo]
        ObjectMapper mapper = new ObjectMapper();
        JsonNode nodeContainer = mapper.readTree(new FileReader("src/test/resources/json/Negociovida.json"));
        assertNotNull(nodeContainer);
        List<Command<?>> commands = readNodeContainer(nodeContainer);
        assertNotNull(commands);
        StatelessKieSession kieSession = kieBase.newStatelessKieSession();
        kieSession.execute(CommandFactory.newBatchExecution(commands));
	}
		
	private List<Command<?>> readNodeContainer(JsonNode nodeContainer) throws Exception{
		List<Command<?>> commands = new ArrayList<Command<?>>();
		if( nodeContainer == null ){
			System.err.println("ERROR LEYENDO EL NODO CONTENEDOR.");
		}else if(nodeContainer.isContainerNode()){
			Iterator<String> fieldNames =  nodeContainer.getFieldNames();
			
	        while(fieldNames.hasNext()){
	        	String field = fieldNames.next();
	        	String nombreClase = ParseUtil.toNombreClase(field);
	        	System.out.println("====== field: "+nombreClase+"=======");
	        	JsonNode jsonNode = nodeContainer.get(nombreClase);
	        	
	        	if(jsonNode != null){
	        		Object objectContainer = createObject(jsonNode, nombreClase);
	        		commands.add(CommandFactory.newInsert(objectContainer));
	        	}else{
	        		System.err.println("NO EXISTE EL NODO: "+nombreClase);
	        	}
	        }
		}
		
		return commands;
	}
	
	private List<Object> createArray(JsonNode arrayNode) throws Exception{
		List<Object> array = new ArrayList<Object>();
		Iterator<JsonNode> elementsArray = arrayNode.iterator();
		while(elementsArray.hasNext()){
			JsonNode arrayElement = elementsArray.next();
			if(arrayElement.isInt()){
				array.add(arrayElement.getIntValue());
			}else if(arrayElement.isTextual()){
				array.add(arrayElement.getTextValue());
			}else if(arrayElement.isDouble()){
				array.add(arrayElement.getDoubleValue());
			}else{
				Iterator<String> fieldElementsArray = arrayElement.getFieldNames();
				while(fieldElementsArray.hasNext()){
					String fieldNameElementArray = fieldElementsArray.next();
					JsonNode nodoArray = arrayElement.get(fieldNameElementArray);
					if(nodoArray.isObject()){
						String nombreClase = ParseUtil.toNombreClase(fieldNameElementArray);
						array.add(createObject(nodoArray, nombreClase));
					}else if(nodoArray.isArray()){
						createArray(arrayNode);
					}else if(nodoArray.isInt()){
						array.add(nodoArray.getIntValue());
					}
				}
				
			}
			
		}
		return array;
	}
	
	
	private Object createObject(JsonNode jsonNode, String objectName) throws Exception{
		FactType factType = findFactType(objectName);
		Object object = factType.newInstance();
		Iterator<String> fields = jsonNode.getFieldNames();
		while(fields.hasNext()){
			String field = fields.next();
			JsonNode node = jsonNode.get(field);
			if(node.isObject()){
				String nombreClase = ParseUtil.toNombreClase(field);
				Object object2 = createObject(node, nombreClase);
				factType.set(object, field, object2);
			}else if(node.isArray()){
				factType.set(object, field, createArray(node));
			}else if(node.isTextual()){
				factType.set(object, field, node.getTextValue());
			}else if(node.isInt()){
				factType.set(object, field, node.getIntValue());
			}else if(node.isDouble()){
				factType.set(object, field, node.getDoubleValue());
			}
		}
		System.out.println("========= Objeto creado: "+objectName+" ======= ");
		return object;
	}
	
	private FactType findFactType(String objectName){
		FactType factType = null;
		for(KiePackage kiePackage: kieBase.getKiePackages()){
			String packageName = kiePackage.getName();
			factType = kieBase.getFactType(packageName, objectName);
			if(factType != null){
				break;
			}
		}
		
		return factType;
	}
	
	
	@Test
	public void testNombreClase(){
		String direccion = "direccion";
		String Direccion = ParseUtil.toNombreClase(direccion);
		assertTrue("Direccion".equals(Direccion));
		assertTrue("Automovil".equals( ParseUtil.toNombreClase("Automovil")));
		assertTrue("Negociovida".equals( ParseUtil.toNombreClase("negocioVida")));
		
	}
	
	
	

}
