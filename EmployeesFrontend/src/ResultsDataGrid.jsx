import React from 'react';

function ResultsDataGrid({ result }) {
  if (!result) return null;

  return (
    <div className="mt-4">
      <div className="card">
        <div className="card-header bg-success text-white">
          <h5 className="mb-0">Results: Longest Working Employee Pair</h5>
        </div>
        <div className="card-body">
          <div className="alert alert-info">
            <strong>🏆 Best Pair:</strong> Employee #{result.empId1} and Employee #{result.empId2} worked
             together for <strong>{result.totalDaysWorked} days</strong> in total
          </div>
          
          <h6 className="mt-3">📊 Common Projects Details:</h6>
          <div className="table-responsive">
            <table className="table table-striped table-hover">
              <thead className="table-dark">
                <tr>
                  <th>Employee ID #1</th>
                  <th>Employee ID #2</th>
                  <th>Project ID</th>
                  <th>Days Worked Together</th>
                </tr>
              </thead>
              <tbody>
                {result.commonProjects && result.commonProjects.map((project, index) => (
                  <tr key={index}>
                    <td>{result.empId1}</td>
                    <td>{result.empId2}</td>
                    <td>{project.projectId}</td>
                    <td>
                      <span className="badge bg-primary">
                        {project.daysWorked} days
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          
          <div className="alert alert-secondary mt-3">
            <small>
              <strong>ℹ️ Note:</strong> Total days ({result.totalDaysWorked}) is the sum of days worked together 
              across all common projects shown above.
            </small>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ResultsDataGrid;