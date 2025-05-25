package com.example.software.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("账单管理控制器测试")
public class BillingViewControllerTest {
    
    private BillingViewController controller;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        controller = new BillingViewController();
    }

    @Test
    @DisplayName("测试导入有效的CSV文件")
    void testImportValidCsvFile() throws IOException {
        // 创建有效的CSV文件
        Path csvFile = tempDir.resolve("valid.csv");
        String validContent = "Category,Product,Date,Time,Price,Remark\n" +
                            "生活费用,食品,2024-03-23,14:30,100.00,午餐\n" +
                            "通讯,话费,2024-03-23,15:00,50.00,手机充值";
        Files.write(csvFile, validContent.getBytes());
        
        assertTrue(controller.validateCsvFormat(csvFile.toString()));
    }

    @Test
    @DisplayName("测试导入无效的CSV文件")
    void testImportInvalidCsvFile() throws IOException {
        // 创建格式错误的CSV文件
        Path csvFile = tempDir.resolve("invalid.csv");
        String invalidContent = "Category,Product\n" +  // 缺少必需的列
                              "生活费用,食品";
        Files.write(csvFile, invalidContent.getBytes());
        
        assertFalse(controller.validateCsvFormat(csvFile.toString()));
    }

    @Test
    @DisplayName("测试导入空CSV文件")
    void testImportEmptyCsvFile() throws IOException {
        // 创建空CSV文件
        Path csvFile = tempDir.resolve("empty.csv");
        Files.write(csvFile, "".getBytes());
        
        assertFalse(controller.validateCsvFormat(csvFile.toString()));
    }

    @Test
    @DisplayName("测试CSV导入的等价类划分")
    void testCsvImportEquivalencePartitioning() throws IOException {
        // 有效等价类 - 标准格式
        Path validFile = tempDir.resolve("validBilling.csv");
        String validContent = "Category,Product,Date,Time,Price,Remark\n" +
                            "生活费用,食品,2024-03-23,14:30,100.00,午餐";
        Files.write(validFile, validContent.getBytes());
        assertTrue(controller.validateCsvFormat(validFile.toString()));

        // 有效等价类 - 中文内容
        Path chineseFile = tempDir.resolve("chineseBilling.csv");
        String chineseContent = "Category,Product,Date,Time,Price,Remark\n" +
                               "生活费用,午餐,2024-03-23,14:30,88.88,好吃的炒面";
        Files.write(chineseFile, chineseContent.getBytes());
        assertTrue(controller.validateCsvFormat(chineseFile.toString()));

        // 无效等价类 - 空文件
        Path emptyFile = tempDir.resolve("empty.csv");
        Files.write(emptyFile, "".getBytes());
        assertFalse(controller.validateCsvFormat(emptyFile.toString()));

        // 无效等价类 - 错误格式
        Path wrongFile = tempDir.resolve("wrongFormat.txt");
        String wrongContent = "这不是一个CSV文件的内容";
        Files.write(wrongFile, wrongContent.getBytes());
        assertFalse(controller.validateCsvFormat(wrongFile.toString()));
    }

    @Test
    @DisplayName("测试账单金额的边界值")
    void testBillingAmountBoundary() {
        // 金额边界值测试
        assertFalse(controller.validateAmount(-0.01)); // 最小值-0.01
        assertTrue(controller.validateAmount(0.00));   // 最小值
        assertTrue(controller.validateAmount(999999.99)); // 有效值
        assertTrue(controller.validateAmount(1000000.00)); // 最大值
        assertFalse(controller.validateAmount(1000000.01)); // 最大值+0.01
    }

    @Test
    @DisplayName("白盒测试 - validateCsvFormat方法的路径覆盖")
    void testValidateCsvFormatPaths() throws IOException {
        // 路径1：文件路径为null
        assertFalse(controller.validateCsvFormat(null), 
            "空文件路径应该返回false");

        // 路径2：文件路径为空字符串
        assertFalse(controller.validateCsvFormat(""), 
            "空字符串路径应该返回false");

        // 路径3：文件不存在
        assertFalse(controller.validateCsvFormat(tempDir.resolve("nonexistent.csv").toString()), 
            "不存在的文件应该返回false");

        // 路径4：文件扩展名不是.csv
        Path txtFile = tempDir.resolve("test.txt");
        Files.write(txtFile, "some content".getBytes());
        assertFalse(controller.validateCsvFormat(txtFile.toString()), 
            "非CSV文件应该返回false");

        // 路径5：空CSV文件
        Path emptyFile = tempDir.resolve("empty.csv");
        Files.write(emptyFile, "".getBytes());
        assertFalse(controller.validateCsvFormat(emptyFile.toString()), 
            "空CSV文件应该返回false");

        // 路径6：缺少必需的列
        Path invalidHeaderFile = tempDir.resolve("invalid_header.csv");
        Files.write(invalidHeaderFile, "Category,Product\n生活费用,食品".getBytes());
        assertFalse(controller.validateCsvFormat(invalidHeaderFile.toString()), 
            "缺少必需列的CSV文件应该返回false");

        // 路径7：无效的日期格式
        Path invalidDateFile = tempDir.resolve("invalid_date.csv");
        String invalidDateContent = "Category,Product,Date,Time,Price,Remark\n" +
                                  "生活费用,食品,invalid-date,14:30,100.00,备注";
        Files.write(invalidDateFile, invalidDateContent.getBytes());
        assertFalse(controller.validateCsvFormat(invalidDateFile.toString()), 
            "包含无效日期的CSV文件应该返回false");

        // 路径8：无效的价格格式
        Path invalidPriceFile = tempDir.resolve("invalid_price.csv");
        String invalidPriceContent = "Category,Product,Date,Time,Price,Remark\n" +
                                   "生活费用,食品,2024-03-23,14:30,invalid-price,备注";
        Files.write(invalidPriceFile, invalidPriceContent.getBytes());
        assertFalse(controller.validateCsvFormat(invalidPriceFile.toString()), 
            "包含无效价格的CSV文件应该返回false");

        // 路径9：完全有效的CSV文件
        Path validFile = tempDir.resolve("valid.csv");
        String validContent = "Category,Product,Date,Time,Price,Remark\n" +
                            "生活费用,食品,2024-03-23,14:30,100.00,备注";
        Files.write(validFile, validContent.getBytes());
        assertTrue(controller.validateCsvFormat(validFile.toString()), 
            "有效的CSV文件应该返回true");
    }

    @Test
    @DisplayName("白盒测试 - validateAmount方法的条件覆盖")
    void testValidateAmountConditions() {
        // 测试所有条件分支
        
        // 条件1：金额小于最小值
        assertFalse(controller.validateAmount(-1.00), 
            "负数金额应该返回false");
        assertFalse(controller.validateAmount(-0.01), 
            "小于最小值的金额应该返回false");
        
        // 条件2：金额等于最小值
        assertTrue(controller.validateAmount(0.00), 
            "最小值金额应该返回true");
        
        // 条件3：金额在有效范围内
        assertTrue(controller.validateAmount(500000.00), 
            "有效范围内的金额应该返回true");
        
        // 条件4：金额等于最大值
        assertTrue(controller.validateAmount(1000000.00), 
            "最大值金额应该返回true");
        
        // 条件5：金额大于最大值
        assertFalse(controller.validateAmount(1000000.01), 
            "超过最大值的金额应该返回false");
        assertFalse(controller.validateAmount(1000001.00), 
            "大于最大值的金额应该返回false");
    }
} 