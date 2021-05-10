//
//  GettingStartedVC.swift
//  Koting
//
//  Created by 임정우 on 2021/03/22.
//

import UIKit
import NVActivityIndicatorView

class GettingStartedVC: UIViewController {
    
    let indicator = CustomIndicator()
    
    // MARK:- View LifeCycle
    override func viewDidLoad() {
        super.viewDidLoad()

    }

    // MARK:- @IBAction func
    @IBAction func startButtonTapped(_ sender: Any) {
        
        
        if checkAccountId() == false {
            
            self.asyncPresentView(identifier: "PhoneAuth")
            
        } else {
            
            indicator.startAnimating(superView: view)
            MailAuthCheckAPI.shared.post() { [weak self] result in
                
                guard let strongSelf = self else { return }
                
                switch result {
                case .success(let mailAuth):
                    
                    let authCheck = mailAuth.result
                    
                    UserDefaults.standard.set(authCheck, forKey: "mailAuthChecked")
                    
                    if authCheck {
                        DispatchQueue.main.async {
                            strongSelf.indicator.stopAnimating()
                            strongSelf.performSegue(withIdentifier: "MeetingList", sender: nil)
                        }
                    } else {
                        DispatchQueue.main.async {
                            strongSelf.indicator.stopAnimating()
                            strongSelf.makeAlertBox(title: "알림", message: "메일 인증을 완료하세요.", text: "확인")
                        }
                    }
                case .failure(let error):
                    DispatchQueue.main.async {
                        strongSelf.indicator.stopAnimating()
                    }
                }
            }
        }
    }
    
    // MARK:- 구현한 함수
    // UserDefaults에 accountId 유무를 check하는 함수
    private func checkAccountId() -> Bool {
        guard let _ = UserDefaults.standard.string(forKey: "accountId") else { return false }
        return true
    }
}
