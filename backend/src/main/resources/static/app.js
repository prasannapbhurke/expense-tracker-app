$(document).ready(function() {
    let expenses = [];
    const categoryColors = {
        'Food': '#ff6384',
        'Transport': '#36a2eb',
        'Shopping': '#cc65fe',
        'Bills': '#ffce56',
        'Other': '#4bc0c0'
    };

    function fetchExpenses() {
        const sortBy = $('#sort-by').val().split(',');
        const range = $('#time-range').val();
        $.ajax({
            url: `/api/expenses?sortBy=${sortBy[0]}&sortDir=${sortBy[1]}&range=${range}`,
            method: 'GET',
            success: function(data) {
                expenses = data;
                renderExpenses();
                updateSummary();
            },
            error: function() {
                window.location.href = '/login.html';
            }
        });
    }

    function renderExpenses() {
        const list = $('#expense-list');
        list.empty();
        const searchTerm = $('#search-input').val().toLowerCase();

        expenses.filter(e => e.name.toLowerCase().includes(searchTerm)).forEach(expense => {
            const date = new Date(expense.timestamp).toLocaleDateString();
            list.append(`
                <tr>
                    <td>
                        <strong>${expense.name}</strong><br>
                        <small class="text-muted">${date} | ${expense.category || 'Uncategorized'}</small>
                    </td>
                    <td class="text-danger font-weight-bold">-Rs ${expense.amount.toFixed(2)}</td>
                    <td class="text-right">
                        <button class="btn btn-sm btn-outline-info edit-btn" data-id="${expense.id}"><i class="fas fa-edit"></i></button>
                        <button class="btn btn-sm btn-outline-danger delete-btn" data-id="${expense.id}"><i class="fas fa-trash"></i></button>
                    </td>
                </tr>
            `);
        });
    }

    function updateSummary() {
        const total = expenses.reduce((sum, exp) => sum + exp.amount, 0);
        $('#total-spent').text(`Rs ${total.toFixed(2)}`);

        // Category breakdown
        const categories = {};
        expenses.forEach(exp => {
            const cat = exp.category || 'Other';
            categories[cat] = (categories[cat] || 0) + exp.amount;
        });

        const summaryBody = $('#category-summary');
        summaryBody.empty();
        for (const cat in categories) {
            const percent = total > 0 ? (categories[cat] / total * 100).toFixed(1) : 0;
            summaryBody.append(`
                <div class="mb-3">
                    <div class="d-flex justify-content-between mb-1">
                        <span class="font-weight-bold text-muted">${cat}</span>
                        <span class="text-dark">Rs ${categories[cat].toFixed(2)} (${percent}%)</span>
                    </div>
                    <div class="progress" style="height: 8px; border-radius: 4px;">
                        <div class="progress-bar" style="width: ${percent}%; background-color: ${categoryColors[cat] || '#ddd'}"></div>
                    </div>
                </div>
            `);
        }
    }

    $('#save-expense').on('click', function() {
        const id = $('#expense-id').val();
        const expense = {
            name: $('#expense-name').val(),
            amount: parseFloat($('#expense-amount').val()),
            category: $('#expense-category').val()
        };

        if(!expense.name || isNaN(expense.amount)) {
            alert("Please enter valid details");
            return;
        }

        const method = id ? 'PUT' : 'POST';
        const url = id ? `/api/expenses/${id}` : '/api/expenses';

        $.ajax({
            url: url,
            method: method,
            contentType: 'application/json',
            data: JSON.stringify(expense),
            success: function() {
                $('#addExpenseModal').modal('hide');
                resetForm();
                fetchExpenses();
            }
        });
    });

    $(document).on('click', '.delete-btn', function() {
        if (confirm('Are you sure you want to delete this expense?')) {
            const id = $(this).data('id');
            $.ajax({
                url: `/api/expenses/${id}`,
                method: 'DELETE',
                success: fetchExpenses
            });
        }
    });

    $(document).on('click', '.edit-btn', function() {
        const id = $(this).data('id');
        const expense = expenses.find(e => e.id == id);
        $('#expense-id').val(expense.id);
        $('#expense-name').val(expense.name);
        $('#expense-amount').val(expense.amount);
        $('#expense-category').val(expense.category);
        $('#addExpenseModalLabel').text('Edit Expense');
        $('#addExpenseModal').modal('show');
    });

    function resetForm() {
        $('#expense-id').val('');
        $('#expense-name').val('');
        $('#expense-amount').val('');
        $('#expense-category').val('Other');
        $('#addExpenseModalLabel').text('Add Expense');
    }

    $('#sort-by').on('change', fetchExpenses);
    $('#time-range').on('change', fetchExpenses);
    $('#search-input').on('input', renderExpenses);

    fetchExpenses();
});
