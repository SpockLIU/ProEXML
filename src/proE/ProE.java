package proE;

import org.dom4j.*;
import org.dom4j.io.*;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

public class ProE {
	
	JFrame jf = new JFrame("xml tool");
	private JTextField filePath= new JTextField(26);
	private JButton upload = new JButton("...");
	private JButton modify = new JButton("Modify");
	JFileChooser chooser = new JFileChooser();
	private JButton setDefault = new JButton("Set Default");
	
	private Document document;
	private SAXReader saxReader = new SAXReader();
	private List<File> xmlFileList = new ArrayList<>();
	private List<Element> dimList = new ArrayList<>();
	
	private String fileLocation = "d:/xml.ini";
	
	private void clearXML(){
//			List list = document.selectNodes("//ProEngData/TolerancedDims/TolerancedDim/TolerancedDimInfos");
			String symLength = null;
			String symView = null;
			String symTol = null;
			Iterator iter = dimList.iterator();
			while(iter.hasNext()){
				Element ele =(Element) iter.next();
				symView = ele.element("TolerancedDimInfos").attribute("ViewFolio").getValue();
				List<Element> comment = ele.element("Comments").elements("Comment");
				for(Element e : comment){
					String refDim = e.attribute("Comment").getValue();
//					System.out.println(refDim);
					if(refDim.contains("+-")){
						symLength = refDim.split("\\s\\+-")[0];
						symTol = refDim.split("\\s\\+-")[1];
//						System.out.println(symLength);
//						System.out.println(symTol);
//						System.out.println(symView);
					}
				}
				List simList = document.selectNodes("//ProEngData/TolerancedDims/TolerancedDim/TolerancedDimInfos");
				Iterator simIter = simList.iterator();
				while(simIter.hasNext()){
					Element lengEle = (Element) simIter.next();
					if(lengEle.attribute("Type").getValue().equals("Sim") || lengEle.attribute("Type").getValue().equals("Psi")){
					String length = lengEle.attribute("Nominal").getValue();
					String lView = lengEle.attribute("ViewFolio").getValue();
					String lTol = lengEle.attribute("TolUpper").getValue();
					String rept = lengEle.attribute("Repetition").getValue();
//					System.out.println(length + "\t" + lTol + "\t" + lView + "\t" + rept);
					if(symLength.equals(length) && symView.equals(lView) && symTol.equals(lTol)){
//						System.out.println(length + "\t" + rept);
						ele.element("TolerancedDimInfos").attribute("Repetition").setValue(rept);
						break;
						}
					}
				}
			}
		
	}
	
	public void findDim(){
		List list = document.selectNodes("//ProEngData/TolerancedDims/TolerancedDim");
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			Element ele = (Element) iter.next();
			List comment = ele.element("Comments").elements("Comment");
			Iterator comIter = comment.iterator();
			while(comIter.hasNext()){
				Element comEle = (Element) comIter.next();
				if(comEle.attribute("Comment").getValue().contains("+-")){
					dimList.add(ele);
//					System.out.println(ele.element("TolerancedDimInfos").attribute("Name").getValue());
//					System.out.println(comEle.attribute("Comment").getValue());
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
			props.load(new FileInputStream(fileLocation));
		} catch (FileNotFoundException e) {
			File iniFile = new File(fileLocation);
			try {
				iniFile.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
			File defaultLocation = new File("C:\\ptc\\pro_soft\\temp");
			if(filePath.getText().trim().length() > 0){
				defaultLocation = new File(filePath.getText().trim());
				
			}
			chooser.setCurrentDirectory(defaultLocation);
			int result = chooser.showDialog(jf, "Choose XML location");
			if(result == JFileChooser.APPROVE_OPTION){
				filePath.setText(chooser.getSelectedFile().getPath());
			}
		});
		jp.add(modify);
		modify.addActionListener(avt ->{
			xmlFileList.clear();
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
//				System.out.println(filePath.getText());
				props.setProperty("path", filePath.getText());
				try {
					OutputStream os = new FileOutputStream(fileLocation);
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
		jf.setResizable(false);
		jf.setSize(550, 75);
		Dimension screenSize =Toolkit.getDefaultToolkit().getScreenSize();
		jf.setLocation((screenSize.width - jf.getWidth()) /2 , (screenSize.height -  jf.getHeight()) / 2);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
	}
	
	private void updateRept(){
			List list = document.selectNodes("/ProEngData/TolerancedDims/TolerancedDim/TolerancedDimInfos");
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
		findDim();
		clearXML();
		updateRept();
		saveAs(file);
	}

	
	public static void main(String[] args) {
		ProE proE = new ProE();
		proE.init();
//		File xmlFile = new File("C:/Users/Spock/Desktop/XML/SE/NVE4984102.xml");
//		proE.readXML(xmlFile);
//		proE.findDim();
//		proE.clearXML();
//		proE.updateRept();
//		proE.saveAs(xmlFile);
	

	}


}
