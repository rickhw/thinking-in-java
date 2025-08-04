import React from 'react';

class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { 
            hasError: false, 
            error: null, 
            errorInfo: null,
            retryCount: 0
        };
    }

    static getDerivedStateFromError(error) {
        // Update state so the next render will show the fallback UI
        return { hasError: true };
    }

    componentDidCatch(error, errorInfo) {
        // Log the error to console for debugging
        console.error('ErrorBoundary caught an error:', error, errorInfo);
        
        // Update state with error details
        this.setState({
            error: error,
            errorInfo: errorInfo
        });

        // You could also log the error to an error reporting service here
        // logErrorToService(error, errorInfo);
    }

    handleRetry = () => {
        this.setState(prevState => ({
            hasError: false,
            error: null,
            errorInfo: null,
            retryCount: prevState.retryCount + 1
        }));
    };

    handleReload = () => {
        window.location.reload();
    };

    render() {
        if (this.state.hasError) {
            // Fallback UI
            return (
                <div className="error-boundary">
                    <div className="error-boundary-content">
                        <div className="error-icon">💥</div>
                        <h2>應用程式發生錯誤</h2>
                        <p>很抱歉，應用程式遇到了意外錯誤。請嘗試重新載入頁面或聯繫技術支援。</p>
                        
                        {/* Show error details in development mode */}
                        {process.env.NODE_ENV === 'development' && this.state.error && (
                            <details className="error-details">
                                <summary>錯誤詳情 (開發模式)</summary>
                                <div className="error-stack">
                                    <h4>錯誤訊息:</h4>
                                    <pre>{this.state.error.toString()}</pre>
                                    
                                    <h4>錯誤堆疊:</h4>
                                    <pre>{this.state.errorInfo.componentStack}</pre>
                                </div>
                            </details>
                        )}
                        
                        <div className="error-boundary-actions">
                            <button 
                                onClick={this.handleRetry}
                                className="retry-button primary"
                                type="button"
                            >
                                重試 {this.state.retryCount > 0 && `(${this.state.retryCount})`}
                            </button>
                            <button 
                                onClick={this.handleReload}
                                className="reload-button secondary"
                                type="button"
                            >
                                重新載入頁面
                            </button>
                        </div>
                        
                        <div className="error-guidance">
                            <h4>如果問題持續發生，請嘗試：</h4>
                            <ul>
                                <li>清除瀏覽器快取和 Cookie</li>
                                <li>檢查網路連線</li>
                                <li>使用其他瀏覽器</li>
                                <li>聯繫技術支援</li>
                            </ul>
                        </div>
                    </div>
                </div>
            );
        }

        // Render children normally when there's no error
        return this.props.children;
    }
}

export default ErrorBoundary;