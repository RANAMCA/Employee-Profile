// API Base URL
const API_BASE_URL = 'http://localhost:8080/api';

// Global state
let currentUser = null;
let allCoworkers = [];
let allFeedbacks = [];
let myFeedbacks = [];
let currentFeedbackView = 'all'; // 'all' or 'mine'
let myLeaveRequests = [];
let pendingApprovals = [];

// Check authentication
const authToken = localStorage.getItem('authToken');
if (!authToken) {
    window.location.href = '/index.html';
}

// Initialize dashboard
document.addEventListener('DOMContentLoaded', async () => {
    displayUserInfo();
    await loadProfile();
    await loadCoworkers();
    await loadFeedback();
    await loadLeaveRequests();
});

// Display user info in header
function displayUserInfo() {
    const fullName = localStorage.getItem('userFullName');
    const role = localStorage.getItem('userRole');

    document.getElementById('userFullName').textContent = fullName || 'User';
    document.getElementById('userRole').textContent = role || 'EMPLOYEE';
}

// Switch tabs
function switchTab(tabName) {
    // Update tab buttons
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');

    // Update tab content
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(content => content.classList.remove('active'));

    if (tabName === 'profile') {
        document.getElementById('profileTab').classList.add('active');
    } else if (tabName === 'coworkers') {
        document.getElementById('coworkersTab').classList.add('active');
    } else if (tabName === 'feedback') {
        document.getElementById('feedbackTab').classList.add('active');
        loadFeedback(); // Reload feedback when tab is switched
    } else if (tabName === 'leave') {
        document.getElementById('leaveTab').classList.add('active');
        loadLeaveRequests(); // Load leave requests when tab is switched
    }
}

// Load user profile
async function loadProfile() {
    const employeeId = localStorage.getItem('employeeId');
    const profileContent = document.getElementById('profileContent');

    try {
        const response = await fetch(`${API_BASE_URL}/employees/${employeeId}`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (response.ok) {
            currentUser = await response.json();
            displayProfile(currentUser);
        } else if (response.status === 401) {
            handleUnauthorized();
        } else {
            profileContent.innerHTML = '<div class="error-message show">Failed to load profile</div>';
        }
    } catch (error) {
        console.error('Error loading profile:', error);
        profileContent.innerHTML = '<div class="error-message show">An error occurred while loading profile</div>';
    }
}

// Display profile information
function displayProfile(employee) {
    const profileContent = document.getElementById('profileContent');

    const html = `
        <div class="profile-field">
            <label>Name</label>
            <div class="value">${employee.firstName} ${employee.lastName}</div>
        </div>
        <div class="profile-field">
            <label>Email</label>
            <div class="value">${employee.email}</div>
        </div>
        <div class="profile-field">
            <label>Position</label>
            <div class="value">${employee.position || 'N/A'}</div>
        </div>
        <div class="profile-field">
            <label>Department</label>
            <div class="value">${employee.department?.name || 'N/A'}</div>
        </div>
        <div class="profile-field">
            <label>Role</label>
            <div class="value">${employee.role?.name || 'N/A'}</div>
        </div>
        <div class="profile-field">
            <label>Phone</label>
            <div class="value ${!employee.phone ? 'not-available' : ''}">${employee.phone || 'Not provided'}</div>
        </div>
        <div class="profile-field">
            <label>Date of Birth</label>
            <div class="value ${!employee.dateOfBirth ? 'not-available' : ''}">${employee.dateOfBirth || 'Not provided'}</div>
        </div>
        <div class="profile-field">
            <label>Hire Date</label>
            <div class="value ${!employee.hireDate ? 'not-available' : ''}">${employee.hireDate || 'Not provided'}</div>
        </div>
        <div class="profile-field" style="grid-column: 1 / -1;">
            <label>Bio</label>
            <div class="value ${!employee.bio ? 'not-available' : ''}">${employee.bio || 'No bio yet'}</div>
        </div>
        <div class="profile-field" style="grid-column: 1 / -1;">
            <label>Skills</label>
            <div class="value ${!employee.skills ? 'not-available' : ''}">${employee.skills || 'No skills listed'}</div>
        </div>
    `;

    profileContent.innerHTML = html;
}

// Load coworkers
async function loadCoworkers() {
    const coworkersContent = document.getElementById('coworkersContent');

    try {
        const response = await fetch(`${API_BASE_URL}/employees`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (response.ok) {
            allCoworkers = await response.json();
            // Filter out current user
            const employeeId = parseInt(localStorage.getItem('employeeId'));
            allCoworkers = allCoworkers.filter(emp => emp.id !== employeeId);
            displayCoworkers(allCoworkers);
        } else if (response.status === 401) {
            handleUnauthorized();
        } else {
            coworkersContent.innerHTML = '<div class="error-message show">Failed to load coworkers</div>';
        }
    } catch (error) {
        console.error('Error loading coworkers:', error);
        coworkersContent.innerHTML = '<div class="error-message show">An error occurred while loading coworkers</div>';
    }
}

// Display coworkers
function displayCoworkers(coworkers) {
    const coworkersContent = document.getElementById('coworkersContent');

    if (coworkers.length === 0) {
        coworkersContent.innerHTML = '<div class="loading">No coworkers found</div>';
        return;
    }

    const html = coworkers.map(emp => `
        <div class="coworker-card">
            <div class="coworker-header">
                <div>
                    <div class="coworker-name">${emp.firstName} ${emp.lastName}</div>
                    <div class="coworker-position">${emp.position || 'N/A'}</div>
                </div>
                <span class="role-badge">${emp.role?.name || 'EMPLOYEE'}</span>
            </div>
            <div class="coworker-info">
                <div class="info-row">
                    <strong>Email:</strong> ${emp.email}
                </div>
                <div class="info-row">
                    <strong>Department:</strong> ${emp.department?.name || 'N/A'}
                </div>
                ${emp.phone ? `
                <div class="info-row">
                    <strong>Phone:</strong> ${emp.phone}
                </div>
                ` : '<div class="info-row" style="color: #999; font-style: italic;">Phone not visible</div>'}
                ${emp.hireDate ? `
                <div class="info-row">
                    <strong>Hire Date:</strong> ${emp.hireDate}
                </div>
                ` : ''}
                ${emp.skills ? `
                <div class="info-row">
                    <strong>Skills:</strong> ${emp.skills}
                </div>
                ` : ''}
            </div>
        </div>
    `).join('');

    coworkersContent.innerHTML = html;
}

// Search coworkers
function searchCoworkers() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();

    const filtered = allCoworkers.filter(emp => {
        const fullName = `${emp.firstName} ${emp.lastName}`.toLowerCase();
        const email = emp.email.toLowerCase();
        const department = emp.department?.name?.toLowerCase() || '';

        return fullName.includes(searchTerm) ||
               email.includes(searchTerm) ||
               department.includes(searchTerm);
    });

    displayCoworkers(filtered);
}

// Edit profile
function editProfile() {
    if (!currentUser) return;

    // Populate form
    document.getElementById('editFirstName').value = currentUser.firstName || '';
    document.getElementById('editLastName').value = currentUser.lastName || '';
    document.getElementById('editPhone').value = currentUser.phone || '';
    document.getElementById('editPosition').value = currentUser.position || '';
    document.getElementById('editBio').value = currentUser.bio || '';
    document.getElementById('editSkills').value = currentUser.skills || '';

    // Show modal
    document.getElementById('editModal').classList.add('show');
}

// Close edit modal
function closeEditModal() {
    document.getElementById('editModal').classList.remove('show');
    document.getElementById('updateError').classList.remove('show');
    document.getElementById('updateSuccess').classList.remove('show');
}

// Handle profile update
async function handleUpdateProfile(event) {
    event.preventDefault();

    const employeeId = localStorage.getItem('employeeId');
    const errorDiv = document.getElementById('updateError');
    const successDiv = document.getElementById('updateSuccess');

    errorDiv.classList.remove('show');
    successDiv.classList.remove('show');

    const updateData = {
        firstName: document.getElementById('editFirstName').value,
        lastName: document.getElementById('editLastName').value,
        phone: document.getElementById('editPhone').value || null,
        position: document.getElementById('editPosition').value || null,
        bio: document.getElementById('editBio').value || null,
        skills: document.getElementById('editSkills').value || null
    };

    try {
        const response = await fetch(`${API_BASE_URL}/employees/${employeeId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updateData)
        });

        if (response.ok) {
            const updatedUser = await response.json();
            currentUser = updatedUser;

            // Update stored full name
            localStorage.setItem('userFullName', `${updatedUser.firstName} ${updatedUser.lastName}`);
            displayUserInfo();

            successDiv.textContent = 'Profile updated successfully!';
            successDiv.classList.add('show');

            // Reload profile display
            displayProfile(currentUser);

            // Close modal after 2 seconds
            setTimeout(() => {
                closeEditModal();
            }, 2000);
        } else if (response.status === 401) {
            handleUnauthorized();
        } else {
            const data = await response.json();
            errorDiv.textContent = data.message || 'Failed to update profile';
            errorDiv.classList.add('show');
        }
    } catch (error) {
        console.error('Error updating profile:', error);
        errorDiv.textContent = 'An error occurred while updating profile';
        errorDiv.classList.add('show');
    }
}

// Handle logout
function handleLogout() {
    localStorage.clear();
    window.location.href = '/index.html';
}

// Handle unauthorized access
function handleUnauthorized() {
    alert('Your session has expired. Please login again.');
    localStorage.clear();
    window.location.href = '/index.html';
}

// Close modal when clicking outside
window.onclick = function(event) {
    const editModal = document.getElementById('editModal');
    const feedbackModal = document.getElementById('feedbackModal');

    if (event.target === editModal) {
        closeEditModal();
    } else if (event.target === feedbackModal) {
        closeFeedbackModal();
    }
}

// ============ FEEDBACK FUNCTIONALITY ============

// Load feedback
async function loadFeedback() {
    const feedbackList = document.getElementById('feedbackList');
    const userRole = localStorage.getItem('userRole');
    const managerControls = document.getElementById('managerFeedbackControls');

    // Show manager controls only for managers
    if (userRole === 'MANAGER') {
        managerControls.style.display = 'block';
    } else {
        managerControls.style.display = 'none';
    }

    try {
        // Load all visible feedbacks
        const response = await fetch(`${API_BASE_URL}/feedbacks/visible`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (response.ok) {
            allFeedbacks = await response.json();

            // For managers, also load their own feedbacks separately
            if (userRole === 'MANAGER') {
                const myResponse = await fetch(`${API_BASE_URL}/feedbacks/my`, {
                    headers: {
                        'Authorization': `Bearer ${authToken}`
                    }
                });

                if (myResponse.ok) {
                    myFeedbacks = await myResponse.json();
                }

                // Update stats
                updateFeedbackStats();
            }

            // Display based on current view
            if (userRole === 'MANAGER' && currentFeedbackView === 'mine') {
                displayFeedback(myFeedbacks);
            } else {
                displayFeedback(allFeedbacks);
            }
        } else if (response.status === 401) {
            handleUnauthorized();
        } else {
            feedbackList.innerHTML = '<div class="error-message show">Failed to load feedback</div>';
        }
    } catch (error) {
        console.error('Error loading feedback:', error);
        feedbackList.innerHTML = '<div class="error-message show">An error occurred while loading feedback</div>';
    }
}

// Update feedback statistics (for managers)
function updateFeedbackStats() {
    document.getElementById('totalFeedbackCount').textContent = allFeedbacks.length;
    document.getElementById('myFeedbackCount').textContent = myFeedbacks.length;
}

// Display feedback list
function displayFeedback(feedbacks) {
    const feedbackList = document.getElementById('feedbackList');

    if (feedbacks.length === 0) {
        feedbackList.innerHTML = `
            <div class="feedback-empty">
                <div class="feedback-empty-icon">💬</div>
                <p>No feedback yet</p>
                <p style="font-size: 14px; margin-top: 8px;">Give feedback to your coworkers to get started!</p>
            </div>
        `;
        return;
    }

    const html = feedbacks.map(fb => {
        const stars = '⭐'.repeat(fb.rating || 0);
        const date = new Date(fb.createdAt).toLocaleDateString();

        return `
            <div class="feedback-item">
                <div class="feedback-header">
                    <div class="feedback-meta">
                        <div class="feedback-to">To: ${fb.employeeName || 'Unknown'}</div>
                        <div class="feedback-from">From: ${fb.authorName || 'Unknown'}</div>
                        <div class="feedback-date">${date}</div>
                    </div>
                    <div class="feedback-rating">${stars}</div>
                </div>
                ${fb.category ? `<div class="feedback-category">${fb.category}</div>` : ''}
                <div class="feedback-content">
                    ${fb.content || 'No content'}
                    ${fb.isPolished ? '<span class="feedback-polished-badge">✨ AI Enhanced</span>' : ''}
                </div>
            </div>
        `;
    }).join('');

    feedbackList.innerHTML = html;
}

// Open give feedback modal
async function openGiveFeedbackModal() {
    const modal = document.getElementById('feedbackModal');
    const employeeSelect = document.getElementById('feedbackEmployee');
    const currentUserRole = localStorage.getItem('userRole');

    // Populate coworker dropdown
    if (allCoworkers.length === 0) {
        await loadCoworkers();
    }

    employeeSelect.innerHTML = '<option value="">Select a coworker</option>';

    // Filter coworkers: Employees cannot give feedback to managers
    const eligibleCoworkers = allCoworkers.filter(emp => {
        // If current user is an employee, exclude managers from the list
        if (currentUserRole === 'EMPLOYEE') {
            return emp.role?.name !== 'MANAGER';
        }
        // Managers can give feedback to anyone
        return true;
    });

    if (eligibleCoworkers.length === 0) {
        employeeSelect.innerHTML = '<option value="">No eligible coworkers found</option>';
    } else {
        eligibleCoworkers.forEach(emp => {
            const option = document.createElement('option');
            option.value = emp.id;
            option.textContent = `${emp.firstName} ${emp.lastName} (${emp.department?.name || 'N/A'})`;
            employeeSelect.appendChild(option);
        });
    }

    // Add character counter
    const contentTextarea = document.getElementById('feedbackContent');
    contentTextarea.addEventListener('input', updateCharCount);

    modal.classList.add('show');
}

// Update character count
function updateCharCount() {
    const textarea = document.getElementById('feedbackContent');
    const charCount = document.getElementById('charCount');
    charCount.textContent = `${textarea.value.length} / 2000 characters`;
}

// Close feedback modal
function closeFeedbackModal() {
    const modal = document.getElementById('feedbackModal');
    modal.classList.remove('show');

    // Clear form
    document.getElementById('feedbackEmployee').value = '';
    document.getElementById('feedbackRating').value = '';
    document.getElementById('feedbackCategory').value = '';
    document.getElementById('feedbackContent').value = '';
    document.getElementById('feedbackPolishAI').checked = true;
    document.getElementById('charCount').textContent = '0 / 2000 characters';

    // Clear messages
    document.getElementById('feedbackError').classList.remove('show');
    document.getElementById('feedbackSuccess').classList.remove('show');
}

// Handle give feedback submission
async function handleGiveFeedback(event) {
    event.preventDefault();

    const errorDiv = document.getElementById('feedbackError');
    const successDiv = document.getElementById('feedbackSuccess');

    errorDiv.classList.remove('show');
    successDiv.classList.remove('show');

    const feedbackData = {
        employeeId: parseInt(document.getElementById('feedbackEmployee').value),
        rating: parseInt(document.getElementById('feedbackRating').value),
        category: document.getElementById('feedbackCategory').value || null,
        content: document.getElementById('feedbackContent').value,
        polishWithAI: document.getElementById('feedbackPolishAI').checked
    };

    try {
        const response = await fetch(`${API_BASE_URL}/feedbacks`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(feedbackData)
        });

        if (response.ok) {
            successDiv.textContent = 'Feedback submitted successfully!';
            successDiv.classList.add('show');

            // Reload feedback list
            await loadFeedback();

            // Close modal after 2 seconds
            setTimeout(() => {
                closeFeedbackModal();
            }, 2000);
        } else if (response.status === 401) {
            handleUnauthorized();
        } else {
            const data = await response.json();
            errorDiv.textContent = data.message || 'Failed to submit feedback';
            errorDiv.classList.add('show');
        }
    } catch (error) {
        console.error('Error submitting feedback:', error);
        errorDiv.textContent = 'An error occurred while submitting feedback';
        errorDiv.classList.add('show');
    }
}

// Switch feedback view (for managers)
function switchFeedbackView(view) {
    currentFeedbackView = view;

    // Update button states
    const allViewBtn = document.getElementById('allViewBtn');
    const myViewBtn = document.getElementById('myViewBtn');

    if (view === 'all') {
        allViewBtn.classList.add('active');
        myViewBtn.classList.remove('active');
        displayFeedback(allFeedbacks);
    } else {
        myViewBtn.classList.add('active');
        allViewBtn.classList.remove('active');
        displayFeedback(myFeedbacks);
    }
}

// ===== LEAVE REQUESTS FUNCTIONALITY =====

// Load leave requests
async function loadLeaveRequests() {
    const userRole = localStorage.getItem('userRole');

    try {
        // Load user's own leave requests
        const myResponse = await fetch(`${API_BASE_URL}/absences/my`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (myResponse.ok) {
            myLeaveRequests = await myResponse.json();
            displayMyLeaveRequests(myLeaveRequests);
        }

        // Load pending approvals for managers
        if (userRole === 'MANAGER') {
            document.getElementById('managerLeaveControls').style.display = 'block';

            const pendingResponse = await fetch(`${API_BASE_URL}/absences/pending`, {
                headers: {
                    'Authorization': `Bearer ${authToken}`
                }
            });

            if (pendingResponse.ok) {
                pendingApprovals = await pendingResponse.json();
                displayPendingApprovals(pendingApprovals);
                updatePendingBadge(pendingApprovals.length);
            }
        } else {
            document.getElementById('managerLeaveControls').style.display = 'none';
        }
    } catch (error) {
        console.error('Error loading leave requests:', error);
    }
}

// Display user's own leave requests
function displayMyLeaveRequests(requests) {
    const container = document.getElementById('myLeaveContent');

    if (!requests || requests.length === 0) {
        container.innerHTML = '<div class="empty-state">You have no leave requests yet.</div>';
        return;
    }

    container.innerHTML = requests.map(leave => `
        <div class="leave-card">
            <div class="leave-card-header">
                <div class="leave-type">${formatLeaveType(leave.type)}</div>
                <span class="leave-status ${leave.status.toLowerCase()}">${leave.status}</span>
            </div>
            <div class="leave-dates">
                <div><strong>From:</strong> ${formatDate(leave.startDate)}</div>
                <div><strong>To:</strong> ${formatDate(leave.endDate)}</div>
            </div>
            <div class="leave-duration">${leave.durationInDays} day${leave.durationInDays > 1 ? 's' : ''}</div>
            ${leave.reason ? `<div class="leave-reason">"${leave.reason}"</div>` : ''}
            ${leave.reviewedBy ? `
                <div class="leave-review">
                    <div class="leave-reviewer">Reviewed by: ${leave.reviewerName || 'Manager'}</div>
                    ${leave.reviewComment ? `<div class="leave-comment">${leave.reviewComment}</div>` : ''}
                </div>
            ` : ''}
            ${leave.status === 'PENDING' ? `
                <div class="leave-actions">
                    <button onclick="cancelLeave(${leave.id})" class="btn btn-cancel-leave">Cancel Request</button>
                </div>
            ` : ''}
        </div>
    `).join('');
}

// Display pending approvals for managers
function displayPendingApprovals(requests) {
    const container = document.getElementById('pendingApprovalsContent');

    if (!requests || requests.length === 0) {
        container.innerHTML = '<div class="empty-state">No pending leave requests to review.</div>';
        return;
    }

    container.innerHTML = requests.map(leave => `
        <div class="leave-card">
            <div class="leave-card-header">
                <div class="leave-type">${formatLeaveType(leave.type)}</div>
                <span class="leave-status ${leave.status.toLowerCase()}">${leave.status}</span>
            </div>
            <div class="leave-employee">Employee: ${leave.employeeName}</div>
            <div class="leave-dates">
                <div><strong>From:</strong> ${formatDate(leave.startDate)}</div>
                <div><strong>To:</strong> ${formatDate(leave.endDate)}</div>
            </div>
            <div class="leave-duration">${leave.durationInDays} day${leave.durationInDays > 1 ? 's' : ''}</div>
            ${leave.reason ? `<div class="leave-reason">"${leave.reason}"</div>` : ''}
            <div class="leave-actions">
                <button onclick="approveLeave(${leave.id})" class="btn btn-approve">Approve</button>
                <button onclick="rejectLeave(${leave.id})" class="btn btn-reject">Reject</button>
            </div>
        </div>
    `).join('');
}

// Open leave request modal
function openLeaveRequestModal() {
    const modal = document.getElementById('leaveRequestModal');
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('leaveStartDate').setAttribute('min', today);
    document.getElementById('leaveEndDate').setAttribute('min', today);
    document.getElementById('leaveError').textContent = '';
    document.getElementById('leaveSuccess').textContent = '';
    modal.style.display = 'block';
}

// Close leave request modal
function closeLeaveRequestModal() {
    const modal = document.getElementById('leaveRequestModal');
    modal.style.display = 'none';
    document.getElementById('leaveType').value = '';
    document.getElementById('leaveStartDate').value = '';
    document.getElementById('leaveEndDate').value = '';
    document.getElementById('leaveReason').value = '';
}

// Handle leave request submission
async function handleLeaveRequest(event) {
    event.preventDefault();

    const errorDiv = document.getElementById('leaveError');
    const successDiv = document.getElementById('leaveSuccess');
    errorDiv.textContent = '';
    successDiv.textContent = '';

    const type = document.getElementById('leaveType').value;
    const startDate = document.getElementById('leaveStartDate').value;
    const endDate = document.getElementById('leaveEndDate').value;
    const reason = document.getElementById('leaveReason').value;

    // Validate dates
    if (new Date(endDate) < new Date(startDate)) {
        errorDiv.textContent = 'End date must be after start date';
        errorDiv.classList.add('show');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/absences`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({
                type,
                startDate,
                endDate,
                reason
            })
        });

        if (response.ok) {
            successDiv.textContent = 'Leave request submitted successfully!';
            successDiv.classList.add('show');

            setTimeout(() => {
                closeLeaveRequestModal();
                loadLeaveRequests(); // Reload leave requests
            }, 1500);
        } else {
            const error = await response.json();

            // Handle validation errors (field-specific errors)
            if (error.errors && typeof error.errors === 'object') {
                const errorMessages = Object.entries(error.errors)
                    .map(([field, message]) => `${field}: ${message}`)
                    .join('\n');
                errorDiv.textContent = errorMessages;
            }
            // Handle array of error messages
            else if (Array.isArray(error.errors)) {
                errorDiv.textContent = error.errors.join('\n');
            }
            // Handle single message
            else {
                errorDiv.textContent = error.message || 'Failed to submit leave request';
            }

            errorDiv.classList.add('show');
        }
    } catch (error) {
        console.error('Error submitting leave request:', error);
        errorDiv.textContent = 'An error occurred. Please try again.';
        errorDiv.classList.add('show');
    }
}

// Approve leave request (manager only)
async function approveLeave(leaveId) {
    const comment = prompt('Add an optional comment (or leave blank):');

    try {
        const response = await fetch(`${API_BASE_URL}/absences/${leaveId}/review`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({
                status: 'APPROVED',
                reviewComment: comment
            })
        });

        if (response.ok) {
            alert('Leave request approved successfully!');
            loadLeaveRequests(); // Reload leave requests
        } else {
            const error = await response.json();
            alert(`Failed to approve: ${error.message || 'Unknown error'}`);
        }
    } catch (error) {
        console.error('Error approving leave:', error);
        alert('An error occurred while approving the request.');
    }
}

// Reject leave request (manager only)
async function rejectLeave(leaveId) {
    const comment = prompt('Please provide a reason for rejection:');

    if (!comment) {
        alert('A reason is required to reject a leave request.');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/absences/${leaveId}/review`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({
                status: 'REJECTED',
                reviewComment: comment
            })
        });

        if (response.ok) {
            alert('Leave request rejected.');
            loadLeaveRequests(); // Reload leave requests
        } else {
            const error = await response.json();
            alert(`Failed to reject: ${error.message || 'Unknown error'}`);
        }
    } catch (error) {
        console.error('Error rejecting leave:', error);
        alert('An error occurred while rejecting the request.');
    }
}

// Cancel leave request (employee only)
async function cancelLeave(leaveId) {
    if (!confirm('Are you sure you want to cancel this leave request?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/absences/${leaveId}/cancel`, {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (response.ok) {
            alert('Leave request cancelled successfully!');
            loadLeaveRequests(); // Reload leave requests
        } else {
            const error = await response.json();
            alert(`Failed to cancel: ${error.message || 'Unknown error'}`);
        }
    } catch (error) {
        console.error('Error cancelling leave:', error);
        alert('An error occurred while cancelling the request.');
    }
}

// Update pending badge count
function updatePendingBadge(count) {
    const badge = document.getElementById('pendingBadge');
    if (count > 0) {
        badge.textContent = count;
        badge.style.display = 'inline-block';
    } else {
        badge.style.display = 'none';
    }
}

// Format date for display
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

// Format leave type for display
function formatLeaveType(type) {
    if (!type) return 'Unknown';
    return type.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
}
