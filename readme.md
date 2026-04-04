# 🏦 Banking Activity Simulation Platform

A **full-stack banking management system** that replicates real-world banking workflows — from user authentication to transaction management — built using **Java (Jersey + MySQL)** and **React (Vite + TypeScript + Tailwind)**.

> 🚀 Developed as part of the **Banking Activity Simulation Internship (Oct 2025)**  
> by **Shreyash Shinde**

---

## 🌟 Recent Highlights & Updates (Oct 2025)

### 🧠 Smarter Authentication
- Enhanced login system:
  - Handles **deactivated users** with a clear message and support contact.
- Added stronger password and field validation on both frontend & backend.

### 💰 Real Transaction Workflow
- Validates PIN, balance, and account status before transfer.  
- Prevents **self-transfers** and **invalid account numbers**.  
- Ensures both sender and receiver accounts are **active**.  
- Generates **unique transaction IDs** with proper tracking.

### 📧 Gmail Notification Integration
- Added **Jakarta Mail SMTP integration** for Gmail.  
- After every successful transaction, both **sender and receiver** get an **email confirmation**.  
- Uses secure SMTP authentication with `bank.simulator.issue@gmail.com` for transaction alerts.

### 📊 Excel Report Generation
- Users and admins can **download all transactions** in Excel format directly from the dashboard.  
- Implemented using **Apache POI** for clean formatting and timestamped exports.  
- API: `/api/transaction/download/all`

### 🧾 Auto Database Initialization
- Introduced `DatabaseInitializerListener.java` to **auto-create database and tables** if not present.  
- Removes need for manual SQL imports.

### ⚙️ Validation & Error Handling
- Added strict validators for Customer, Account, and Transaction entities.  
- Ensures valid Aadhar (12 digits), phone number, and 6-digit PIN.  
- Centralized and reusable error handling using `ApiResponse.java`.

### 🧑‍💻 Developer Experience
- Cleaner code organization and improved logs for debugging.  
- JUnit + Mockito testing for controller, service, and validator layers.  
- Modular architecture separating concerns for easy scaling.

---

## 🧩 Tech Stack

### **Backend**
- **Language:** Java 22  
- **Framework:** Jersey (Jakarta RESTful Web Services 3.1.3)  
- **Database:** MySQL  
- **Build Tool:** Maven  
- **Testing:** JUnit 5, Mockito  
- **Excel Export:** Apache POI  
- **Mailing:** Jakarta Mail (Gmail SMTP)  

### **Frontend**
- **Framework:** React (Vite + TypeScript)  
- **Styling:** Tailwind CSS  
- **UI Library:** Shadcn/UI  
- **Build Tools:** Vite, ESLint  

---

## 📁 Project Structure

```
shreyashs19-bank-simulator-intern/
├── backend/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/java/com/bank/simulator/
│   │   │   ├── config/           # DBConfig, CORS, Auto DB Setup
│   │   │   ├── controller/       # Auth, Account, Customer, Transaction APIs
│   │   │   ├── model/            # Entity Classes (User, Account, Transaction, etc.)
│   │   │   ├── service/          # Business Logic + Email + Excel Export
│   │   │   ├── validation/       # Input Validators
│   │   └── test/                 # JUnit & Mockito Tests
│   └── webapp/WEB-INF/web.xml
│
└── frontend/
    ├── src/
    │   ├── pages/                # UI Pages (Login, Dashboard, Accounts, etc.)
    │   ├── components/           # Reusable Components & Modals
    │   ├── services/             # API Integrations (Axios)
    │   ├── utils/                # Excel Export, Toasts, etc.
    │   └── hooks/                # Custom React Hooks
    ├── public/
    ├── package.json
    ├── vite.config.ts
    └── tailwind.config.ts
```

---

## ⚙️ Backend Setup

### **Requirements**
- Java 22+
- Maven 3.9+
- MySQL (port 3306)
- Gmail account for mail notifications

### **Steps**
```bash
cd backend
mvn clean package
mvn jetty:run
```

✅ Auto creates database `bank_simulation` and required tables.  
Backend runs on:
```
http://localhost:8080/api
```

### **Mail Configuration**
In `application.properties`, set your Gmail credentials:
```properties
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.username=your-email@gmail.com
mail.password=your-app-password
```

*(Use a Gmail App Password — not your regular password.)*

---

## 🌐 Frontend Setup

### **Requirements**
- Node.js 18+

### **Steps**
```bash
cd frontend
npm install
cp .env.example .env
```

Set your backend API URL:
```
VITE_API_URL=http://localhost:8080/api
```

Run the development server:
```bash
npm run dev
```

Frontend will be available at:
```
http://localhost:5173
```

---

## 🔗 Key API Endpoints

| Method | Endpoint | Description |
|--------|-----------|--------------|
| **POST** | `/api/auth/signup` | Register a new user |
| **POST** | `/api/auth/login` | Authenticate user |
| **GET** | `/api/auth/users/all` | Get all users |
| **PUT** | `/api/auth/user/status` | Activate / Deactivate user |
| **POST** | `/api/customer/onboard` | Add new customer |
| **GET** | `/api/customer/all` | List all customers |
| **PUT** | `/api/customer/aadhar/{aadhar}` | Update customer by Aadhar |
| **POST** | `/api/account/add` | Create a new account |
| **GET** | `/api/account/all` | View all accounts |
| **POST** | `/api/transaction/createTransaction` | Create a transaction (triggers email + balance update) |
| **GET** | `/api/transaction/download/all` | Download all transactions as Excel |

---

## 🧪 Testing

Run all unit tests:
```bash
cd backend
mvn test
```

Includes:
- **Controller Tests** — API validation  
- **Service Tests** — Business logic and DB mock testing  
- **Validation Tests** — Input validation and error handling  

---

## 🧰 Troubleshooting

| Issue | Solution |
|--------|-----------|
| `MySQL JDBC Driver not found` | Ensure dependency is present in `pom.xml`. |
| `Gmail SMTP authentication failed` | Enable 2-Step verification and create an App Password. |
| `Frontend not connecting` | Check `.env` for correct `VITE_API_URL`. |
| `Port 8080 already in use` | Update port in `application.properties`. |
| `CORS blocked` | Ensure `CorsFilter.java` is registered with `@Provider`. |

---

## 📜 License

This project is licensed under the [MIT License](./LICENSE.txt).  
© 2025 **Shreyash Shinde** — All rights reserved.

---

> 💡 *“Code that simulates reality is one step closer to innovation.”*
