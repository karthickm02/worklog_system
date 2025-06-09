# Worklog System - Database Schema

## Table Definitions

### users
| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier for the user |
| email | VARCHAR(255) | NOT NULL, UNIQUE | User's email address |
| first_name | VARCHAR(100) | NOT NULL | User's first name |
| last_name | VARCHAR(100) | NOT NULL | User's last name |
| role | ENUM('employee', 'manager', 'admin') | NOT NULL | User's role in the system |
| department_id | UUID | FOREIGN KEY REFERENCES departments(id) | User's department |
| password_hash | VARCHAR(255) | NOT NULL | Hashed password |
| join_date | DATE | NOT NULL | User's join date |
| status | ENUM('active', 'inactive', 'suspended') | NOT NULL, DEFAULT 'active' | User's current status |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record update timestamp |

**Indexes:**
- PRIMARY KEY (id)
- UNIQUE INDEX idx_users_email (email)
- INDEX idx_users_department (department_id)
- INDEX idx_users_status (status)

**Relationships:**
- One-to-Many with `time_entries` (one user has many time entries)
- Many-to-One with `departments` (many users belong to one department)
- One-to-Many with `reports` (one user generates many reports)

### departments
| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier for the department |
| name | VARCHAR(100) | NOT NULL | Department name |
| manager_id | UUID | FOREIGN KEY REFERENCES users(id) | Department manager |
| cost_center | VARCHAR(50) | NOT NULL | Cost center code |
| location | VARCHAR(100) | NOT NULL | Department location |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record update timestamp |

**Indexes:**
- PRIMARY KEY (id)
- INDEX idx_departments_manager (manager_id)
- INDEX idx_departments_cost_center (cost_center)

**Relationships:**
- One-to-Many with `users` (one department has many users)
- Many-to-One with `users` via manager_id (department has one manager)

### time_entries
| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier for the time entry |
| user_id | UUID | FOREIGN KEY REFERENCES users(id) | User who created the entry |
| clock_in | TIMESTAMP | NOT NULL | Clock in time |
| clock_out | TIMESTAMP | NULL | Clock out time |
| break_duration | INTEGER | NOT NULL, DEFAULT 0 | Break duration in minutes |
| notes | TEXT | NULL | Optional notes |
| status | ENUM('pending', 'approved', 'rejected') | NOT NULL, DEFAULT 'pending' | Entry status |
| project_id | UUID | FOREIGN KEY REFERENCES projects(id) | Associated project |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record update timestamp |

**Indexes:**
- PRIMARY KEY (id)
- INDEX idx_time_entries_user (user_id)
- INDEX idx_time_entries_project (project_id)
- INDEX idx_time_entries_dates (clock_in, clock_out)
- INDEX idx_time_entries_status (status)

**Relationships:**
- Many-to-One with `users` (many entries belong to one user)
- Many-to-One with `projects` (many entries associated with one project)

### projects
| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier for the project |
| name | VARCHAR(100) | NOT NULL | Project name |
| department_id | UUID | FOREIGN KEY REFERENCES departments(id) | Associated department |
| status | ENUM('active', 'completed', 'archived') | NOT NULL, DEFAULT 'active' | Project status |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record update timestamp |

**Indexes:**
- PRIMARY KEY (id)
- INDEX idx_projects_department (department_id)
- INDEX idx_projects_status (status)

**Relationships:**
- Many-to-One with `departments` (many projects belong to one department)
- One-to-Many with `time_entries` (one project has many time entries)

### reports
| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier for the report |
| type | ENUM('daily', 'monthly', 'yearly', 'custom') | NOT NULL | Report type |
| period_start | DATE | NOT NULL | Start date of report period |
| period_end | DATE | NOT NULL | End date of report period |
| generated_by | UUID | FOREIGN KEY REFERENCES users(id) | User who generated the report |
| department_id | UUID | FOREIGN KEY REFERENCES departments(id) | Department scope of report |
| status | ENUM('pending', 'approved', 'rejected') | NOT NULL, DEFAULT 'pending' | Report status |
| data | JSONB | NOT NULL | Report data in JSON format |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record update timestamp |

**Indexes:**
- PRIMARY KEY (id)
- INDEX idx_reports_generated_by (generated_by)
- INDEX idx_reports_department (department_id)
- INDEX idx_reports_period (period_start, period_end)
- INDEX idx_reports_type_status (type, status)

**Relationships:**
- Many-to-One with `users` (many reports generated by one user)
- Many-to-One with `departments` (many reports for one department)

### holidays
| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier for the holiday |
| name | VARCHAR(100) | NOT NULL | Holiday name |
| date | DATE | NOT NULL | Holiday date |
| type | ENUM('public', 'company', 'department') | NOT NULL | Holiday type |
| department_id | UUID | FOREIGN KEY REFERENCES departments(id), NULL | Department (if department-specific) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record update timestamp |

**Indexes:**
- PRIMARY KEY (id)
- INDEX idx_holidays_date (date)
- INDEX idx_holidays_department (department_id)

**Relationships:**
- Many-to-One with `departments` (many holidays can be specific to one department)

### time_off_requests
| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier for the request |
| user_id | UUID | FOREIGN KEY REFERENCES users(id) | Requesting user |
| start_date | DATE | NOT NULL | Leave start date |
| end_date | DATE | NOT NULL | Leave end date |
| type | ENUM('vacation', 'sick', 'personal', 'other') | NOT NULL | Leave type |
| status | ENUM('pending', 'approved', 'rejected') | NOT NULL, DEFAULT 'pending' | Request status |
| approved_by | UUID | FOREIGN KEY REFERENCES users(id), NULL | Approving manager |
| notes | TEXT | NULL | Request notes |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record update timestamp |

**Indexes:**
- PRIMARY KEY (id)
- INDEX idx_time_off_requests_user (user_id)
- INDEX idx_time_off_requests_dates (start_date, end_date)
- INDEX idx_time_off_requests_status (status)

**Relationships:**
- Many-to-One with `users` (many requests belong to one user)
- Many-to-One with `users` via approved_by (many requests approved by one manager)

## PostgreSQL DDL Statements

```sql
-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create ENUM types
CREATE TYPE user_role AS ENUM ('employee', 'manager', 'admin');
CREATE TYPE user_status AS ENUM ('active', 'inactive', 'suspended');
CREATE TYPE time_entry_status AS ENUM ('pending', 'approved', 'rejected');
CREATE TYPE project_status AS ENUM ('active', 'completed', 'archived');
CREATE TYPE report_type AS ENUM ('daily', 'monthly', 'yearly', 'custom');
CREATE TYPE report_status AS ENUM ('pending', 'approved', 'rejected');
CREATE TYPE holiday_type AS ENUM ('public', 'company', 'department');
CREATE TYPE time_off_type AS ENUM ('vacation', 'sick', 'personal', 'other');
CREATE TYPE time_off_status AS ENUM ('pending', 'approved', 'rejected');

-- Create tables
CREATE TABLE departments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    manager_id UUID,
    cost_center VARCHAR(50) NOT NULL,
    location VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role user_role NOT NULL,
    department_id UUID REFERENCES departments(id),
    password_hash VARCHAR(255) NOT NULL,
    join_date DATE NOT NULL,
    status user_status NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key to departments after users table exists
ALTER TABLE departments 
ADD CONSTRAINT fk_departments_manager 
FOREIGN KEY (manager_id) REFERENCES users(id);

CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    department_id UUID REFERENCES departments(id),
    status project_status NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE time_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    clock_in TIMESTAMP NOT NULL,
    clock_out TIMESTAMP,
    break_duration INTEGER NOT NULL DEFAULT 0,
    notes TEXT,
    status time_entry_status NOT NULL DEFAULT 'pending',
    project_id UUID REFERENCES projects(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type report_type NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    generated_by UUID REFERENCES users(id),
    department_id UUID REFERENCES departments(id),
    status report_status NOT NULL DEFAULT 'pending',
    data JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE holidays (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    type holiday_type NOT NULL,
    department_id UUID REFERENCES departments(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE time_off_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    type time_off_type NOT NULL,
    status time_off_status NOT NULL DEFAULT 'pending',
    approved_by UUID REFERENCES users(id),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_department ON users(department_id);
CREATE INDEX idx_users_status ON users(status);

CREATE INDEX idx_departments_manager ON departments(manager_id);
CREATE INDEX idx_departments_cost_center ON departments(cost_center);

CREATE INDEX idx_time_entries_user ON time_entries(user_id);
CREATE INDEX idx_time_entries_project ON time_entries(project_id);
CREATE INDEX idx_time_entries_dates ON time_entries(clock_in, clock_out);
CREATE INDEX idx_time_entries_status ON time_entries(status);

CREATE INDEX idx_projects_department ON projects(department_id);
CREATE INDEX idx_projects_status ON projects(status);

CREATE INDEX idx_reports_generated_by ON reports(generated_by);
CREATE INDEX idx_reports_department ON reports(department_id);
CREATE INDEX idx_reports_period ON reports(period_start, period_end);
CREATE INDEX idx_reports_type_status ON reports(type, status);

CREATE INDEX idx_holidays_date ON holidays(date);
CREATE INDEX idx_holidays_department ON holidays(department_id);

CREATE INDEX idx_time_off_requests_user ON time_off_requests(user_id);
CREATE INDEX idx_time_off_requests_dates ON time_off_requests(start_date, end_date);
CREATE INDEX idx_time_off_requests_status ON time_off_requests(status);
```

## Prisma Schema (ORM Example)

```prisma
enum UserRole {
  employee
  manager
  admin
}

enum UserStatus {
  active
  inactive
  suspended
}

enum TimeEntryStatus {
  pending
  approved
  rejected
}

enum ProjectStatus {
  active
  completed
  archived
}

enum ReportType {
  daily
  monthly
  yearly
  custom
}

enum ReportStatus {
  pending
  approved
  rejected
}

enum HolidayType {
  public
  company
  department
}

enum TimeOffType {
  vacation
  sick
  personal
  other
}

enum TimeOffStatus {
  pending
  approved
  rejected
}

model User {
  id            String    @id @default(uuid())
  email         String    @unique
  firstName     String
  lastName      String
  role          UserRole
  department    Department? @relation(fields: [departmentId], references: [id])
  departmentId  String?
  passwordHash  String
  joinDate      DateTime
  status        UserStatus @default(active)
  createdAt     DateTime   @default(now())
  updatedAt     DateTime   @updatedAt

  // Relations
  timeEntries   TimeEntry[]
  managedDepartment Department[] @relation("DepartmentManager")
  generatedReports Report[]
  timeOffRequests TimeOffRequest[] @relation("RequestedBy")
  approvedRequests TimeOffRequest[] @relation("ApprovedBy")
}

model Department {
  id          String    @id @default(uuid())
  name        String
  manager     User      @relation("DepartmentManager", fields: [managerId], references: [id])
  managerId   String
  costCenter  String
  location    String
  createdAt   DateTime  @default(now())
  updatedAt   DateTime  @updatedAt

  // Relations
  users       User[]
  projects    Project[]
  reports     Report[]
  holidays    Holiday[]
}

model TimeEntry {
  id            String    @id @default(uuid())
  user          User      @relation(fields: [userId], references: [id])
  userId        String
  clockIn       DateTime
  clockOut      DateTime?
  breakDuration Int       @default(0)
  notes         String?
  status        TimeEntryStatus @default(pending)
  project       Project?  @relation(fields: [projectId], references: [id])
  projectId     String?
  createdAt     DateTime  @default(now())
  updatedAt     DateTime  @updatedAt
}

model Project {
  id            String    @id @default(uuid())
  name          String
  department    Department @relation(fields: [departmentId], references: [id])
  departmentId  String
  status        ProjectStatus @default(active)
  createdAt     DateTime  @default(now())
  updatedAt     DateTime  @updatedAt

  // Relations
  timeEntries   TimeEntry[]
}

model Report {
  id            String    @id @default(uuid())
  type          ReportType
  periodStart   DateTime
  periodEnd     DateTime
  generatedBy   User      @relation(fields: [generatedById], references: [id])
  generatedById String
  department    Department @relation(fields: [departmentId], references: [id])
  departmentId  String
  status        ReportStatus @default(pending)
  data          Json
  createdAt     DateTime  @default(now())
  updatedAt     DateTime  @updatedAt
}

model Holiday {
  id            String    @id @default(uuid())
  name          String
  date          DateTime
  type          HolidayType
  department    Department? @relation(fields: [departmentId], references: [id])
  departmentId  String?
  createdAt     DateTime  @default(now())
  updatedAt     DateTime  @updatedAt
}

model TimeOffRequest {
  id            String    @id @default(uuid())
  user          User      @relation("RequestedBy", fields: [userId], references: [id])
  userId        String
  startDate     DateTime
  endDate       DateTime
  type          TimeOffType
  status        TimeOffStatus @default(pending)
  approvedBy    User?     @relation("ApprovedBy", fields: [approvedById], references: [id])
  approvedById  String?
  notes         String?
  createdAt     DateTime  @default(now())
  updatedAt     DateTime  @updatedAt
}
``` 