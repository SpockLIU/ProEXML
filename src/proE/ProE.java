package proE;

import org.dom4j.*;
import org.dom4j.io.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

public class ProE {
	
	JFrame jf = new JFrame("xml转换程式");
	private JTextField filePath= new JTextField(26);
	private JButton upload = new JButton("...");
	private JButton modify = new JButton("Modify");
	JFileChooser chooser = new JFileChooser();
	private JButton setDefault = new JButton("Set Deafult");
	
	private Document document;
	private SAXReader saxReader = new SAXReader();
	private List<File> xmlFileList = new ArrayList<>();
	
	private void clearXML(){
			List list = document.selectNodes("//ProEngData/TolerancedDims/TolerancedDim/TolerancedDimInfos");
			Iterator iter = list.iterator();
			while(iter.hasNext()){
				Element element =(Element) iter.next();
				if(element.attribute("Type").getValue().equals("Sym")){
//					System.out.println(element.attribute("Name").getValue());
					Element symEle = element.getParent().element("Comments").element("Comment");	
					String symLength = symEle.attribute("Comment").getValue().split("\\s")[0];
					String symView = element.attribute("ViewFolio").getValue();
					String symTol = symEle.attribute("Comment").getValue().split("\\s")[1].split("-")[1];
					List simList = document.selectNodes("//ProEngData/TolerancedDims/TolerancedDim/TolerancedDimInfos");
					Iterator simIter = simList.iterator();
					while(simIter.hasNext()){
						Element lengEle = (Element) simIter.next();
						if(lengEle.attribute("Type").getValue().equals("Sim")){
						String length = lengEle.attribute("Nominal").getValue();
						String lView = lengEle.attribute("ViewFolio").getValue();
						String lTol = lengEle.attribute("TolUpper").getValue();
						String rept = lengEle.attribute("Repetition").getValue();
						if(symLength.equals(length) && symView.equals(lView) && symTol.equals(lTol)){
							element.attribute("Repetition").setValue(rept);
						}
						}
					}
				}
			}
		
	}
	
	public void createXMLlist(String preFile){
		File xmlPath = new File(preFile);
		if(!xmlPath.exists()){
			try {
				xmlPath.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(xmlPath.isDirectory()){
			for(File file : xmlPath.listFiles()){
				String name = file.getName();
				Pattern pattern = Pattern.compile("[a-z]{3}\\d{7}\\.xml", Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(name);
				if(matcher.matches()){
					xmlFileList.add(file);
				}
			}
		}else {
			System.out.println("You need select a file, not a path");
		}
	}
	
	private void readXML(File xmlFile){
		try {
			document = saxReader.read(xmlFile);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	public void init(){
		
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(".\\xml.ini"));
		} catch (FileNotFoundException e) {
			File iniFile = new File("\\xml.ini");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String path = props.getProperty("path");
		filePath.setText(path);
		filePath.setEditable(true);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		JPanel jp = new JPanel();
		jp.add(filePath);
		jp.add(upload);
		upload.addActionListener(event -> {
			chooser.setCurrentDirectory(new File(path));
			int result = chooser.showDialog(jf, "OK");
			if(result == JFileChooser.APPROVE_OPTION){
				filePath.setText(chooser.getSelectedFile().getPath());
			}
		});
		jp.add(modify);
		modify.addActionListener(avt ->{
			if(filePath.getText().trim().length() > 0){
				createXMLlist(filePath.getText());
				for(File file : xmlFileList){
					createNewXML(file);
				}
			}
		});
		jp.add(setDefault);
		setDefault.addActionListener(event -> {
			if(filePath.getText().trim().length() > 0){
				System.out.println(filePath.getText());
				props.setProperty("path", filePath.getText());
				try {
					OutputStream os = new FileOutputStream("./xml.ini");
					props.store(os, null);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		});
		jf.add(jp);
		jf.setSize(600, 100);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
	}
	
	private void updateRept(){
			List list = document.selectNodes("//ProEngData/TolerancedDims/TolerancedDim/TolerancedDimInfos");
			for(int i = list.size() - 1; i >=0; i--){
				Element element = (Element) list.get(i);
				Attribute name = element.attribute("Name");
				Attribute rept = element.attribute("Repetition");
				int reptInt = Integer.parseInt(rept.getValue());
				if(reptInt > 0){
					//System.out.println(name.getValue());
					//System.out.println(rept.getValue());
					Element parent = element.getParent().getParent();
					for(int j = reptInt; j > 0; j--){
						Element cloneE = (Element) element.getParent().clone();
						String newName = name.getValue() + "-" + j;
						cloneE.element("TolerancedDimInfos").attribute("Name").setValue(newName);
						cloneE.element("TolerancedDimInfos").attribute("Repetition").setValue("0");
						parent.elements().add(i + 1, cloneE);
					}
					parent.elements().remove(i);
				}
				
			}
			
	}
	
	private String newFileName(File file){
		String fileName = file.getName();
		String parent = file.getParent();
		String newFileName = fileName.split("\\.")[0] + "-new." +  fileName.split("\\.")[1];
		return (parent + "/" +newFileName);
	}

	private void saveAs(File inputXML){
		try{
			File outputXML = new File(newFileName(inputXML));
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter output = new XMLWriter(new FileWriter(outputXML), format);
			output.write(document);
			output.close();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public void createNewXML(File file){
		readXML(file);
		clearXML();
		updateRept();
		saveAs(file);
	}
	
	public void test(){
		init();
	}
	
	public static void main(String[] args) {
		ProE proE = new ProE();
		proE.test();

	}


}
