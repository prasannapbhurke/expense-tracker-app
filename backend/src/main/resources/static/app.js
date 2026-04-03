$(document).ready(function() {
    const expenseList = $('#expense-list');
    const addExpenseForm = $('#add-expense-form');
    const saveExpenseButton = $('#save-expense');
    const sortBy = $('#sort-by');
    let editExpenseId = null;

    function loadExpenses() {
        const sortValue = sortBy.val().split(',');
        const sortParam = `?sortBy=${sortValue[0]}&sortDir=${sortValue[1]}`;

        $.ajax({
            url: `/api/expenses${sortParam}`,
            method: 'GET',
            success: function(data) {
                expenseList.empty();
                data.forEach(function(expense) {
                    expenseList.append(`
                        <tr>
                            <td>${expense.name}</td>
                            <td>${expense.amount}</td>
                            <td>
                                <button class="btn btn-sm btn-primary edit-expense" data-id="${expense.id}">Edit</button>
                                <button class="btn btn-sm btn-danger delete-expense" data-id="${expense.id}">Delete</button>
                            </td>
                        </tr>
                    `);
                });
            }
        });
    }

    sortBy.on('change', loadExpenses);

    saveExpenseButton.on('click', function() {
        const name = $('#expense-name').val();
        const amount = $('#expense-amount').val();
        const expense = { name: name, amount: amount };

        let url = '/api/expenses';
        let method = 'POST';
        if (editExpenseId) {
            url += `/${editExpenseId}`;
            method = 'PUT';
        }

        $.ajax({
            url: url,
            method: method,
            contentType: 'application/json',
            data: JSON.stringify(expense),
            success: function() {
                loadExpenses();
                addExpenseForm[0].reset();
                $('#addExpenseModal').modal('hide');
                editExpenseId = null;
            }
        });
    });

    expenseList.on('click', '.edit-expense', function() {
        const id = $(this).data('id');
        editExpenseId = id;
        $.ajax({
            url: `/api/expenses/${id}`,
            method: 'GET',
            success: function(expense) {
                $('#expense-name').val(expense.name);
                $('#expense-amount').val(expense.amount);
                $('#addExpenseModal').modal('show');
            }
        });
    });

    expenseList.on('click', '.delete-expense', function() {
        const id = $(this).data('id');
        $.ajax({
            url: `/api/expenses/${id}`,
            method: 'DELETE',
            success: function() {
                loadExpenses();
            }
        });
    });

    $('#addExpenseModal').on('hidden.bs.modal', function () {
        addExpenseForm[0].reset();
        editExpenseId = null;
    });

    loadExpenses();
});
