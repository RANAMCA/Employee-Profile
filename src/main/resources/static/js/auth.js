// API Base URL
const API_BASE_URL = 'http://localhost:8080/api';

// Check if already logged in
if (localStorage.getItem('authToken')) {
    window.location.href = '/dashboard.html';
}

// Load departments on page load
document.addEventListener('DOMContentLoaded', () => {
    loadDepartments();
});

// Load departments from API
async function loadDepartments() {
    const departmentSelect = document.getElementById('registerDepartment');

    try {
        const response = await fetch(`${API_BASE_URL}/departments/public`);

        if (response.ok) {
            const departments = await response.json();

            // Clear loading option
            departmentSelect.innerHTML = '';

            // Add default option
            const defaultOption = document.createElement('option');
            defaultOption.value = '';
            defaultOption.textContent = 'Select a department';
            departmentSelect.appendChild(defaultOption);

            // Add department options
            departments.forEach(dept => {
                const option = document.createElement('option');
                option.value = dept.id;
                option.textContent = `${dept.name}${dept.location ? ' - ' + dept.location : ''}`;
                departmentSelect.appendChild(option);
            });
        } else {
            console.error('Failed to load departments');
            departmentSelect.innerHTML = '<option value="">Failed to load departments</option>';
        }
    } catch (error) {
        console.error('Error loading departments:', error);
        departmentSelect.innerHTML = '<option value="">Error loading departments</option>';
    }
}

// Switch between login and register forms
function switchToRegister() {
    document.getElementById('loginForm').classList.remove('active');
    document.getElementById('registerForm').classList.add('active');
    clearMessages();
}

function switchToLogin() {
    document.getElementById('registerForm').classList.remove('active');
    document.getElementById('loginForm').classList.add('active');
    clearMessages();
}

// Clear error/success messages
function clearMessages() {
    const messages = document.querySelectorAll('.error-message, .success-message');
    messages.forEach(msg => {
        msg.classList.remove('show');
        msg.textContent = '';
    });
}

// Handle Login
async function handleLogin(event) {
    event.preventDefault();

    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    const errorDiv = document.getElementById('loginError');

    clearMessages();

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            // Store auth data
            localStorage.setItem('authToken', data.token);
            localStorage.setItem('refreshToken', data.refreshToken);
            localStorage.setItem('employeeId', data.employeeId);
            localStorage.setItem('userEmail', data.email);
            localStorage.setItem('userFullName', data.fullName);
            localStorage.setItem('userRole', data.roleName);

            // Redirect to dashboard
            window.location.href = '/dashboard.html';
        } else {
            errorDiv.textContent = data.message || 'Login failed. Please check your credentials.';
            errorDiv.classList.add('show');
        }
    } catch (error) {
        console.error('Login error:', error);
        errorDiv.textContent = 'An error occurred. Please try again.';
        errorDiv.classList.add('show');
    }
}

// Handle Registration
async function handleRegister(event) {
    event.preventDefault();

    const firstName = document.getElementById('registerFirstName').value;
    const lastName = document.getElementById('registerLastName').value;
    const email = document.getElementById('registerEmail').value;
    const password = document.getElementById('registerPassword').value;
    const position = document.getElementById('registerPosition').value;
    const departmentId = parseInt(document.getElementById('registerDepartment').value);

    const errorDiv = document.getElementById('registerError');
    const successDiv = document.getElementById('registerSuccess');

    clearMessages();

    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                firstName,
                lastName,
                email,
                password,
                position,
                departmentId
            })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            successDiv.textContent = 'Registration successful! Redirecting to login...';
            successDiv.classList.add('show');

            // Clear form
            event.target.reset();

            // Switch to login after 2 seconds
            setTimeout(() => {
                switchToLogin();
            }, 2000);
        } else {
            errorDiv.textContent = data.message || 'Registration failed. Please try again.';
            errorDiv.classList.add('show');
        }
    } catch (error) {
        console.error('Registration error:', error);
        errorDiv.textContent = 'An error occurred. Please try again.';
        errorDiv.classList.add('show');
    }
}
