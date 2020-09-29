/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.demo.creditscorer;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sun.util.calendar.CalendarUtils;

public class CreditScorer {
    // project diretctory please change to desired one
    String outputFolder = "/Users/retina/weloveprojects/creditScorer/data.json"; 
    // project diretctory please change to desired one
    String scoreFile = "/Users/retina/weloveprojects/creditScorer"; 
  
    JSONArray credits_data  = new JSONArray();
    FileWriter file;
    
    void generateData(){
        
      
      
       int acc_min = 100000000;
       int acc_max = 999999999;
       

       float income_min = 0;
       float income_max = 10;
       
       int loans_min = 0;
       int loans_max = 5;
       
       int dependants_min = 0;
       int dependants_max = 5;
       
        try {
            file = new FileWriter(outputFolder);
        } catch (Exception e) {
             e.printStackTrace();
        }
       
    //    I have made 150,000 so as the file can be small
      for(int i=0;i<150000;i++){
          
            int account_number = (int)(Math.random() * (acc_max - acc_min + 1) + acc_min);
            float monthly_income = (int)(Math.random() * (income_max - income_min + 1) + income_min);   
            int loans = (int)(Math.random() * (loans_max - loans_min + 1) + loans_min);
            int dependants = (int)(Math.random() * (dependants_max - dependants_min + 1) + dependants_min);
            
            JSONObject account_data = new JSONObject();
            account_data.put("account_number", account_number);
            account_data.put("monthly_income", monthly_income);
            account_data.put("loans", loans);
            account_data.put("dependants", dependants);
            credits_data.add(account_data);
            System.out.println("Sample data created: "+account_data);
            
        }
        
      //Write JSON file
        try (FileWriter file = new FileWriter(outputFolder)) {
            file.write(credits_data.toJSONString());
            file.flush();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
      
    }
    
    public int  processData(){
        JSONParser jsonParser = new JSONParser();
         
        try (FileReader reader = new FileReader(outputFolder))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
 
            JSONArray credit_data = (JSONArray) obj; 
             
            JSONArray thread_data = new JSONArray();
            //Iterate over employee array
            int count = 0;
            for (;count<credit_data.size();count++){ 
                
                if(count %500 == 0){
                   //feed to thread
                   creditScore score_batch = new creditScore(thread_data);
                   thread_data = new JSONArray();
                   //start thread
                   score_batch.start();
                   
                }else{
                    thread_data.add((JSONObject) credit_data.get(count));
                }
                
            }; 
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        
       return 0;
    }
      
 
class creditScore extends Thread 
{ 

    JSONArray credit_data;
    JSONArray score_data = new JSONArray();
    
    public creditScore(JSONArray data){
        this.credit_data = data;
    }
    
    public void run() 
    { 
        try
        { 
            
            // Displaying the thread that is running 
            System.out.println ("Thread " + 
                  Thread.currentThread().getId() + 
                  " is running"); 
            for(int i=0;i<this.credit_data.size();i++){
                JSONObject customer_data = (JSONObject) this.credit_data.get(i); 
                //scoring algoritm score = monthly_income - loans - dependants
                double score = (double)customer_data.get("monthly_income")-
                        (Long)customer_data.get("loans")-
                        (Long)customer_data.get("dependants");
                if(score < 0){
                    score = 1;
                }
                
                JSONObject customer_score = new JSONObject();
                customer_score.put("account_number", customer_data.get("account_number"));
                customer_score.put("score", score);
                
                score_data.add(customer_score);
            }
            System.out.println("Score: "+this.score_data);
  
             FileWriter file = new FileWriter(scoreFile+Thread.currentThread().getId()+"-scores.json");
             file.write(this.score_data.toJSONString());
             file.flush();
             file.close();
        } 
        catch (Exception e) 
        { 
            // Throwing an exception 
            System.out.println ("Exception is caught"); 
            e.printStackTrace();
        } 
    }
}
    
    public static void main(String args[]){
        CreditScorer cs = new CreditScorer();
        
        
       cs.generateData();
       long start = System.currentTimeMillis(); 
       cs.processData();
       long end  = System.currentTimeMillis();
       float sec = (end - start) / 1000F;  
       System.out.println("Elapsed Time:"+ sec + " seconds");
        
    }

     
}
