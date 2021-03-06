/**
 *  Copyright 2010 Health Information Systems Project of India
 *
 *  This file is part of RKS module.
 *
 *  RKS module is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  RKS module is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with RKS module.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/

package org.openmrs.module.rks.web.controller.item;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.openmrs.api.context.Context;
import org.openmrs.module.rks.RKSService;
import org.openmrs.module.rks.model.Category;
import org.openmrs.module.rks.model.Item;
import org.openmrs.module.rks.util.DateUtils;
import org.openmrs.module.rks.util.PagingUtil;
import org.openmrs.module.rks.util.RequestUtil;
import org.openmrs.module.rks.web.controller.category.CategoryPropertyEditor;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;


@Controller("RKSItemController")
public class ItemController {
	 Log log = LogFactory.getLog(this.getClass());
	@RequestMapping(value="/module/rks/item.form", method=RequestMethod.GET)
	public String view(@RequestParam(value="itemId",required=false) Integer  itemId,@ModelAttribute("item") Item item, Model model){
		RKSService rksService =Context.getService(RKSService.class);
		if(itemId != null){
			item = rksService.getItemById(itemId);
			model.addAttribute("item",item);
		}
		model.addAttribute("transactionTypes",Item.TRANSACTION_NAMES);
		return "/module/rks/item/form";
	}
	
	@ModelAttribute("subCategories")
	public List<Category> parents(){
		RKSService rksService =Context.getService(RKSService.class);
		List<Category> subCategories = rksService.listCategory("", 0, 0);
		return subCategories;
	}
	
	@ModelAttribute("transactionTypes")
	public String[] transactionTypes(){
		return Item.TRANSACTION_NAMES;
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Category.class, new CategoryPropertyEditor());
		binder.registerCustomEditor(String.class,"dateIncomeOutcome", new CustomDateEditor(new SimpleDateFormat("dd/MM/yyyy"), false));
	}
	@RequestMapping(value="/module/rks/item.form", method=RequestMethod.POST)
	public String post(@ModelAttribute("item") Item item,BindingResult bindingResult, SessionStatus status, Model model){
		new ItemValidator().validate(item, bindingResult);
		if (bindingResult.hasErrors()) {
			return "/module/rks/item/form";
		}else{
		RKSService rksService =Context.getService(RKSService.class);
		item.setCreatedOn(new java.util.Date());
		item.setCreatedBy(Context.getAuthenticatedUser().getGivenName());
		rksService.saveItem(item);
		status.setComplete();
		return "redirect:/module/rks/item.form";
		}
	}
	@RequestMapping(value="/module/rks/itemList.form", method=RequestMethod.GET)
	public String list( 
			@RequestParam(value="pageSize",required=false)  Integer pageSize, 
            @RequestParam(value="currentPage",required=false)  Integer currentPage,
            @RequestParam(value="searchName",required=false)  String name,
            @RequestParam(value="transactionType",required=false)  String transactionType,
            @RequestParam(value="categoryId",required=false)  Integer categoryId,
            @RequestParam(value="fromDate",required=false)  String fromDate,
            @RequestParam(value="toDate",required=false)  String toDate,
            Map<String, Object> model, HttpServletRequest request){
		RKSService rksService =Context.getService(RKSService.class);
		
		

		
		int total = rksService.countListItem(categoryId, name, transactionType, fromDate, toDate);
		PagingUtil pagingUtil = new PagingUtil( RequestUtil.getCurrentLink(request) , pageSize, currentPage, total );
		List<Item> items =rksService.listItem(categoryId, name, transactionType, fromDate, toDate, pagingUtil.getStartPos(), pagingUtil.getPageSize());

		//process excel here
		
		List<Category> categories = rksService.listCategory("",   0, 0);
		model.put("categories",categories);
		model.put("categoryId",categoryId);
		model.put("transactionTypes",Item.TRANSACTION_NAMES);

		model.put("items", items );
		model.put("pagingUtil", pagingUtil);
		model.put("fromDate", fromDate);
		model.put("toDate", toDate);
		model.put("transactionType", transactionType);
		model.put("searchName", name);
		
		
		
		return "/module/rks/item/list";
	}
	
	@RequestMapping(value="/module/rks/itemExport.form")
	public void excelExport( 
			@RequestParam(value="pageSize",required=false)  Integer pageSize, 
            @RequestParam(value="currentPage",required=false)  Integer currentPage,
            @RequestParam(value="searchName",required=false)  String name,
            @RequestParam(value="transactionType",required=false)  String transactionType,
            @RequestParam(value="categoryId",required=false)  Integer categoryId,
            @RequestParam(value="fromDate",required=false)  String fromDate,
            @RequestParam(value="toDate",required=false)  String toDate,
            @RequestParam(value="exportType",required=false)  String export,
            Map<String, Object> model, HttpServletRequest request,  HttpServletResponse response) throws Exception{
		RKSService rksService =Context.getService(RKSService.class);
		List<Item> items =rksService.listItem(categoryId, name, transactionType, fromDate, toDate, 0, 0);
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=rks"+"_"+DateUtils.getCurrentDate()+".xls"); 		
		//process excel here
		HSSFWorkbook wb =  excelFile(items);
		OutputStream out = response.getOutputStream();
	    wb.write(out);
	    out.close();
		
	}
	
	@RequestMapping(value="/module/rks/itemList.form", method=RequestMethod.POST)
    public String deleteStores(@RequestParam("ids") String[] ids,HttpServletRequest request){
		String temp = "";
    	HttpSession httpSession = request.getSession();
		Integer itemId  = null;
		try{
			RKSService rksService =Context.getService(RKSService.class);
			if( ids != null && ids.length > 0 ){
				for(String sId : ids )
				{
					itemId = Integer.parseInt(sId);
					if( itemId!= null && itemId > 0)
					{
						rksService.deleteItem(itemId);
					}else{
						//temp += "We can't delete store="+store.getName()+" because that store is using please check <br/>";
						temp = "This item cannot be deleted as it is in use";
					}
				}
			}
		}catch (Exception e) {
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
			"Can not delete item ");
			log.error(e);
		}
		httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, StringUtils.isBlank(temp) ?  "item.deleted" : temp);
    	
    	return "redirect:/module/rks/itemList.form";
    }
	private HSSFWorkbook excelFile(List<Item> list){
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		//sheet.setDefaultColumnWidth((short) 25);
		HSSFFont f = wb.createFont();
		f.setFontHeight((short) 200);
		f.setFontName("Arial");
		HSSFFont cellFont = wb.createFont();
		cellFont.setFontHeight((short) 200);
		cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		cellFont.setColor(org.apache.poi.hssf.util.HSSFColor.GREEN.index);
		cellFont.setFontName("Arial");

		HSSFCell cellcol1 = null;
		HSSFCell cellcol2 = null;
		HSSFCell cellcol3 = null;
		HSSFCell cellcol4 = null;
		HSSFCell cellcol5 = null;
		HSSFCell cellcol6 = null;
		HSSFCell cellcol7 = null;
		HSSFCell cellcol8 = null;
		HSSFCell cellcol9 = null;
		
		
		// put text in first cell
		cellcol1 = getCell(sheet, 0, 0);
		cellcol2 = getCell(sheet, 0, 1);
		cellcol3 = getCell(sheet, 0, 2);
		cellcol4 = getCell(sheet, 0, 3);
		cellcol5 = getCell(sheet, 0, 4);
		cellcol6 = getCell(sheet, 0, 5);
		cellcol7 = getCell(sheet, 0, 6);
		cellcol8 = getCell(sheet, 0, 7);
		cellcol9 = getCell(sheet, 0, 8);
		
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setFillForegroundColor(HSSFColor.GREEN.index);
		style.setFillBackgroundColor(HSSFColor.BLACK.index);

		setText(cellcol1, "No");
		setText(cellcol2, "Sub's category"); 	
		setText(cellcol3, "Category");
		setText(cellcol4, "Transaction type");
		setText(cellcol5, "Date income|outcome");
		setText(cellcol6, "Amount");
		setText(cellcol7, "Description");
		setText(cellcol8, "Created on");
		setText(cellcol9, "Created by");
		
		cellcol1.setCellStyle(style);
		cellcol2.setCellStyle(style);
		cellcol3.setCellStyle(style);
		cellcol4.setCellStyle(style);
		cellcol5.setCellStyle(style);
		cellcol6.setCellStyle(style);
		cellcol7.setCellStyle(style);
		cellcol8.setCellStyle(style);
		cellcol9.setCellStyle(style);
		
		int row=1;
		BigDecimal totalIncome= new BigDecimal(0);
		BigDecimal totalOutcome=  new BigDecimal(0);
		if(CollectionUtils.isNotEmpty(list)){
	    	for(int i =0;i<list.size();i++){
	    		Item item = list.get(i);
	    		//No
	    		cellcol1 = getCell(sheet, row, 0);
	    		String temp = (i+1) +"";
				setText(cellcol1, temp);
				//Sub's category
				cellcol2 = getCell(sheet, row, 1);
				setText(cellcol2, item.getCategory().getName());
				//parent
				cellcol3 = getCell(sheet, row, 2);
				setText(cellcol3, item.getCategory().getName());
				//transaction type
				cellcol4 = getCell(sheet, row, 3);
				setText(cellcol4, item.getTransactionType());
				//Date income|outcome
				cellcol5 = getCell(sheet, row, 4);
				setText(cellcol5, DateUtils.formatterDDMMYYYY.format(item.getDateIncomeOutcome()));
				//Amout
				cellcol6 = getCell(sheet, row, 5);
				setText(cellcol6, item.getAmount()+"");
				//Description
				cellcol7 = getCell(sheet, row, 6);
				setText(cellcol7, item.getDescription());
				//Created on
				cellcol8 = getCell(sheet, row, 7);
				setText(cellcol8,DateUtils.formatterDDMMYYYY.format(item.getCreatedOn()));
				//Created by
				cellcol9 = getCell(sheet, row, 8);
				setText(cellcol9,item.getCreatedBy());
				row++;
				
				if( item.getTransactionType().equalsIgnoreCase(Item.TRANSACTION_NAMES[0])){
					totalIncome = totalIncome.add(item.getAmount());
				}
				if( item.getTransactionType().equalsIgnoreCase(Item.TRANSACTION_NAMES[1])){
					totalOutcome = totalOutcome.add(item.getAmount());
				}
	    	}
		}
		
		//set total income
		row=row+2;
		cellcol5 = getCell(sheet, row, 4);
		setText(cellcol5, "Income sources: ");
		//Amout
		cellcol6 = getCell(sheet, row, 5);
		setText(cellcol6, totalIncome+"");
		
		//set total income
		row=row+1;
		cellcol5 = getCell(sheet, row, 4);
		setText(cellcol5, "Expenditure sources: ");
		//Amout
		cellcol6 = getCell(sheet, row, 5);
		setText(cellcol6, totalOutcome+"");
		
		/*HSSFWorkbook wb          = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		HSSFRow row     = sheet.createRow((short)0); 
		HSSFCell cell   = row.createCell((short)0); 
		cell.setCellValue(1); 
		//set header
		row.createCell((short)1).setCellValue(""); 
		row.createCell((short)2).setCellValue("This is a string");
		row.createCell((short)3).setCellValue("");*/
		
		
		return wb;
	}
	private void setText(HSSFCell cell, String text) {
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(text);
	}
	private HSSFCell getCell(HSSFSheet sheet, int row, int col) {
		HSSFRow sheetRow = sheet.getRow(row);
		if (sheetRow == null) {
			sheetRow = sheet.createRow(row);
		}
		HSSFCell cell = sheetRow.getCell((short) col);
		if (cell == null) {
			cell = sheetRow.createCell((short) col);
		}
		return cell;
	}
	
}
