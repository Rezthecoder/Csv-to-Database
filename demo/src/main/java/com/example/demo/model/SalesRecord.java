package com.example.demo.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesRecord {

  private String country;
  private String itemType;
  private String orderId;
  private String unitsSold;
  private String unitPrice;
  private String unitCost;
}
