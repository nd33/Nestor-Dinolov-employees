import React from 'react'

function FileUploader({ onFileSelect, onAnalyze, selectedFile, loading }) {
  return (
    <div>
      <div className="mb-3">
        <label htmlFor="formFile" className="form-label fw-bold">
          📁 Select CSV File
        </label>
        <input
          className="form-control"
          type="file"
          id="formFile"
          accept=".csv"
          onChange={onFileSelect}
          disabled={loading}
        />
        <div className="form-text text-muted mt-2">
          File should have columns: EmpID, ProjectID, DateFrom, DateTo<br/>
          <strong>Example row:</strong> 143, 12, 2013-11-01, 2014-01-05<br/>
          <strong>Note:</strong> DateTo can be NULL (means today)
        </div>
      </div>
      
      <button 
        className="btn btn-primary w-100"
        onClick={onAnalyze}
        disabled={!selectedFile || loading}
      >
        {loading ? (
          <>
            <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            Analyzing...
          </>
        ) : (
          '🔍 Analyze Employee Pairs'
        )}
      </button>
    </div>
  )
}

export default FileUploader