# Financial Management System User Manual

## Introduction

**Financial Management System** is a desktop financial management application that supports multi-user login, bill management, savings plans, and data import/export features, helping users efficiently manage personal or household finances.

## Directory Structure

```
├── Financial management system.exe      # Windows executable
├── software.jar                        # Java executable package
├── Financial management system.xml      # Launch configuration file
├── icon.ico                            # Application icon
├── data/                               # Data directory
│   ├── users.json                      # User information
│   ├── savings_plans.json              # Savings plans
│   ├── transactions.log                # Operation logs
│   └── billing/                        # Billing data
│       ├── billingEntries.json         # Billing details (recommended)
│       ├── billingEntries.csv          # Billing details (CSV format)
│       ├── billingEntries.txt          # Billing details (text format)
│       └── test.txt                    # Test file
```

## Installation and Running

### Environment Requirements

- Windows 7/10/11 (recommended to run the EXE directly)
- Java 8 or higher (if using the JAR package)（jdk17 is the best option）

### Launch Methods

1. **Windows Users**  
   Double-click `Financial management system.exe` to start the application.

2. **Cross-platform/Command-line Users**  
   Alternatively, Run the following command in the terminal:
   ```
   java -jar software.jar
   ```
3.**Running the Ollama Model**

To run the Ollama model, follow these steps:

1. **Install Ollama**: Ensure you have Ollama installed on your system. You can download it from the [official Ollama website](https://ollama.ai/).

2. **Run the Model**: Use the following command in your terminal to start the Ollama model:
   ```bash
   ollama run deepseek-r1:7b
   ```

3. **Access the Model**: Once running, you can interact with the model through the provided interface or API.

For more detailed instructions, refer to the [Ollama documentation](https://ollama.ai/docs).

## Main Features

### 1. User Management

- Supports multi-user registration and login.
- User information is stored in `data/users.json`, including username, encrypted password, email, etc.

### 2. Bill Management

- Supports income and expense categorization.
- Billing data is stored in `data/billing/billingEntries.json`, with support for multiple import/export formats (JSON/CSV/TXT).
- Bill fields include: category, product, price, date, time, remarks, etc.

### 3. Savings Plans

- Create and manage various savings plans (e.g., daily, weekly, monthly cycles).
- Savings plan data is stored in `data/savings_plans.json`, including plan name, cycle, amount, associated user, etc.

### 4. Data Import/Export

- Supports importing and exporting billing, user, and savings plan data for backup and migration.

### 5. Logging and Security

- Operation logs are recorded in `data/transactions.log`.
- User passwords are stored encrypted to ensure data security.

## Data Format Specifications

### User Information (users.json)

```json
[
  {
    "userId": "uuid",
    "username": "username",
    "password": "encrypted_password",
    "email": "",
    "phone": null,
    "userSettings": null
  }
]
```

### Billing Details (billingEntries.json)

```json
[
  {
    "entryId": null,
    "category": "category",
    "product": "product",
    "price": amount,
    "date": "YYYY-MM-DD",
    "time": "HH:mm",
    "formattedTime": "HH:mm",
    "remark": "remarks"
  }
]
```

### Savings Plans (savings_plans.json)

```json
[
  {
    "planId": "uuid",
    "name": "plan_name",
    "startDate": "start_date",
    "endDate": "end_date",
    "cycle": "cycle_type",
    "cycleTimes": times,
    "amount": single_amount,
    "totalAmount": total_amount,
    "savedAmount": saved_amount,
    "currency": "currency",
    "user": { ...user_info... }
  }
]
```

## Frequently Asked Questions

1. **Unable to start the application?**  
   Ensure that Java 8 or higher is installed, or use the EXE file directly.

2. **Data loss/corruption?**  
   It is recommended to regularly back up all files in the `data/` directory.

3. **Forgot password?**  
   Currently, please contact the administrator or manually modify `users.json`. Future versions will support password recovery.

## Contact and Support

For questions or suggestions, please contact the developer or submit an issue on the project homepage.
